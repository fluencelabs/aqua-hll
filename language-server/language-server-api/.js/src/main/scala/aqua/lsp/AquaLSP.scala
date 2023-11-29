package aqua.lsp

import aqua.compiler.*
import aqua.compiler.AquaError.{ParserError as AquaParserError, *}
import aqua.compiler.AquaWarning.*
import aqua.files.{AquaFileSources, AquaFilesIO, FileModuleId}
import aqua.io.*
import aqua.parser.lexer.{LiteralToken, Token}
import aqua.parser.lift.FileSpan.F
import aqua.parser.lift.{FileSpan, Span}
import aqua.parser.{ArrowReturnError, BlockIndentError, LexerError, ParserError}
import aqua.raw.ConstantRaw
import aqua.semantics.rules.locations.TokenInfo
import aqua.semantics.{HeaderError, RulesViolated, SemanticWarning, WrongAST}
import aqua.types.{LiteralType, ScalarType}
import aqua.{AquaIO, SpanParser}

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.option.*
import fs2.io.file.{Files, Path}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.JSConverters.*
import scala.scalajs.js.annotation.*
import scala.scalajs.js.{UndefOr, undefined}
import scribe.Logging

@JSExportAll
case class CompilationResult(
  errors: js.Array[ErrorInfo],
  warnings: js.Array[WarningInfo] = js.Array(),
  locations: js.Array[TokenLink] = js.Array(),
  importLocations: js.Array[TokenImport] = js.Array(),
  tokens: js.Array[TokenInfoJs] = js.Array()
)

@JSExportAll
case class TokenInfoJs(location: TokenLocation, `type`: String)

@JSExportAll
case class TokenLocation(name: String, startLine: Int, startCol: Int, endLine: Int, endCol: Int)

@JSExportAll
case class TokenLink(current: TokenLocation, definition: TokenLocation)

@JSExportAll
case class TokenImport(current: TokenLocation, path: String)

object TokenLocation {

  def fromSpan(span: FileSpan): Option[TokenLocation] = {
    val start = span.locationMap.value.toLineCol(span.span.startIndex)
    val end = span.locationMap.value.toLineCol(span.span.endIndex)

    for {
      startLC <- start
      endLC <- end
    } yield {
      TokenLocation(span.name, startLC._1, startLC._2, endLC._1, endLC._2)
    }

  }
}

@JSExportAll
case class ErrorInfo(
  start: Int,
  end: Int,
  message: String,
  location: UndefOr[String]
) {
  // Used to distinguish from WarningInfo in TS
  val infoType: String = "error"
}

object ErrorInfo {

  def apply(fileSpan: FileSpan, message: String): ErrorInfo = {
    val start = fileSpan.span.startIndex
    val end = fileSpan.span.endIndex
    ErrorInfo(start, end, message, fileSpan.name)
  }

  def applyOp(start: Int, end: Int, message: String, location: Option[String]): ErrorInfo = {
    ErrorInfo(start, end, message, location.getOrElse(undefined))
  }
}

@JSExportAll
case class WarningInfo(
  start: Int,
  end: Int,
  message: String,
  location: UndefOr[String]
) {
  // Used to distinguish from ErrorInfo in TS
  val infoType: String = "warning"
}

object WarningInfo {

  def apply(fileSpan: FileSpan, message: String): WarningInfo = {
    val start = fileSpan.span.startIndex
    val end = fileSpan.span.endIndex
    WarningInfo(start, end, message, fileSpan.name)
  }
}

@JSExportTopLevel("AquaLSP")
object AquaLSP extends App with Logging {

  private def errorToInfo(
    error: AquaError[FileModuleId, AquaFileError, FileSpan.F]
  ): List[ErrorInfo] = error match {
    case AquaParserError(err) =>
      err match {
        case BlockIndentError(indent, message) =>
          ErrorInfo(indent._1, message) :: Nil
        case ArrowReturnError(point, message) =>
          ErrorInfo(point._1, message) :: Nil
        case LexerError((span, e)) =>
          e.expected.toList
            .groupBy(_.offset)
            .map { case (offset, exps) =>
              val localSpan = Span(offset, offset + 1)
              val fSpan = FileSpan(span.name, span.locationMap, localSpan)
              val errorMessages = exps.flatMap(exp => ParserError.expectationToString(exp))
              val msg = s"${errorMessages.head}" :: errorMessages.tail.map(t => "OR " + t)
              (offset, ErrorInfo(fSpan, msg.mkString("\n")))
            }
            .toList
            .sortBy(_._1)
            .map(_._2)
            .reverse
      }
    case SourcesError(err) =>
      ErrorInfo.applyOp(0, 0, err.showForConsole, None) :: Nil
    case ResolveImportsError(_, token, err) =>
      ErrorInfo(token.unit._1, err.showForConsole) :: Nil
    case ImportError(token) =>
      ErrorInfo(token.unit._1, "Cannot resolve import") :: Nil
    case CycleError(modules) =>
      ErrorInfo.applyOp(
        0,
        0,
        s"Cycle loops detected in imports: ${modules.map(_.file.fileName)}",
        None
      ) :: Nil
    case CompileError(err) =>
      err match {
        case RulesViolated(token, messages) =>
          ErrorInfo(token.unit._1, messages.mkString("\n")) :: Nil
        case HeaderError(token, message) =>
          ErrorInfo(token.unit._1, message) :: Nil
        case WrongAST(ast) =>
          ErrorInfo.applyOp(0, 0, "Semantic error: wrong AST", None) :: Nil

      }
    case OutputError(_, err) =>
      ErrorInfo.applyOp(0, 0, err.showForConsole, None) :: Nil
    case AirValidationError(errors) =>
      errors.toChain.toList.map(ErrorInfo.applyOp(0, 0, _, None))
  }

  private def warningToInfo(
    warning: AquaWarning[FileSpan.F]
  ): List[WarningInfo] = warning match {
    case CompileWarning(SemanticWarning(token, messages)) =>
      WarningInfo(token.unit._1, messages.mkString("\n")) :: Nil
  }

  @JSExport
  def compile(
    pathStr: String,
    imports: scalajs.js.Array[String]
  ): scalajs.js.Promise[CompilationResult] = {
    logger.debug(s"Compiling '$pathStr' with imports: $imports")

    given AquaIO[IO] = new AquaFilesIO[IO]

    val path = Path(pathStr)
    val pathId = FileModuleId(path)
    val sources = new AquaFileSources[IO](path, imports.toList.map(Path.apply))
    val config = AquaCompilerConf(ConstantRaw.defaultConstants(None))

    val proc = for {
      res <- LSPCompiler.compileToLsp[IO, AquaFileError, FileModuleId, FileSpan.F](
        sources,
        SpanParser.parser,
        config
      )
    } yield {
      val fileRes = res.andThen(
        _.get(pathId).toValidNec(
          SourcesError(Unresolvable(s"Unexpected. No file $pathStr in compiler results"))
        )
      )

      logger.debug("Compilation done.")

      def tokensToJs(tokens: List[TokenInfo[FileSpan.F]]): js.Array[TokenInfoJs] = {
        tokens.flatMap { ti =>
          TokenLocation.fromSpan(ti.token.unit._1).map { tl =>
            val typeName = ti.`type` match {
              case LiteralType(oneOf, _) if oneOf == ScalarType.integer =>
                "u32"
              case LiteralType(oneOf, _) if oneOf == ScalarType.float =>
                "f32"
              case LiteralType(oneOf, _) if oneOf == ScalarType.string =>
                "string"
              case LiteralType(oneOf, _) if oneOf == ScalarType.bool =>
                "bool"
              case t => t.toString
            }
            TokenInfoJs(tl, typeName)
          }
        }.toJSArray
      }

      def locationsToJs(
        locations: List[(Token[FileSpan.F], Token[FileSpan.F])]
      ): js.Array[TokenLink] = {
        locations.flatMap { case (from, to) =>
          val fromOp = TokenLocation.fromSpan(from.unit._1)
          val toOp = TokenLocation.fromSpan(to.unit._1)

          val link = for {
            from <- fromOp
            to <- toOp
          } yield TokenLink(from, to)

          if (link.isEmpty)
            logger.warn(s"Incorrect coordinates for token '${from.unit._1.name}'")

          link.toList
        }.toJSArray
      }

      def importsToTokenImport(imports: List[LiteralToken[FileSpan.F]]): js.Array[TokenImport] =
        imports.flatMap { lt =>
          val (span, str) = lt.valueToken
          val unquoted = str.substring(1, str.length - 1)
          TokenLocation.fromSpan(span).map(l => TokenImport(l, unquoted))
        }.toJSArray

      val result = fileRes match {
        case Valid(lsp) =>
          val errors = lsp.errors.map(CompileError.apply).flatMap(errorToInfo)
          val warnings = lsp.warnings.map(CompileWarning.apply).flatMap(warningToInfo)
          errors match
            case Nil =>
              logger.debug("No errors on compilation.")
            case errs =>
              logger.debug("Errors: " + errs.mkString("\n"))

          CompilationResult(
            errors.toJSArray,
            warnings.toJSArray,
            locationsToJs(lsp.locations),
            importsToTokenImport(lsp.importTokens),
            tokensToJs(lsp.tokens.values.toList)
          )
        case Invalid(e) =>
          val errors = e.toChain.toList.flatMap(errorToInfo)
          logger.debug("Errors: " + errors.mkString("\n"))
          CompilationResult(errors.toJSArray)
      }
      result
    }

    proc.unsafeToFuture().toJSPromise

  }
}
