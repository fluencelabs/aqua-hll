package aqua

import aqua.model.LiteralModel
import aqua.model.transform.GenerationConfig
import aqua.parser.expr.ConstantExpr
import aqua.parser.lift.LiftParser
import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.effect.{unsafe, ExitCode, IO}
import cats.effect.std.Console
import cats.syntax.functor.*
import cats.syntax.traverse.*
import cats.{Comonad, Functor}
import com.monovore.decline.Opts.help
import com.monovore.decline.{Opts, Visibility}
import scribe.Level
import fs2.io.file.{Files, Path}

object AppOps {

  val helpOpt: Opts[Unit] =
    Opts.flag("help", help = "Display this help text", "h", Visibility.Partial).asHelp.as(())

  val versionOpt: Opts[Unit] =
    Opts.flag("version", help = "Show version", "v", Visibility.Partial)

  val logLevelOpt: Opts[Level] =
    Opts.option[String]("log-level", help = "Set log level").withDefault("info").mapValidated {
      str =>
        Validated.fromEither(toLogLevel(str))
    }

  def toLogLevel(logLevel: String): Either[NonEmptyList[String], Level] = {
    LogLevel.stringToLogLevel
      .get(logLevel.toLowerCase)
      .toRight(
        NonEmptyList(
          "log-level could be only 'all', 'trace', 'debug', 'info', 'warn', 'error', 'off'",
          Nil
        )
      )
  }

  def checkPath(implicit runtime: unsafe.IORuntime): String => ValidatedNel[String, Path] = {
    pathStr =>
      Validated
        .fromEither(Validated.catchNonFatal {
          val p = Path(pathStr)
          Files[IO]
            .exists(p)
            .flatMap { exists =>
              if (exists)
                Files[IO].isRegularFile(p).map { isFile =>
                  if (isFile) {
                    val filename = p.fileName.toString
                    val ext = Option(filename)
                      .filter(_.contains("."))
                      .map(f => f.substring(f.lastIndexOf(".") + 1))
                      .getOrElse("")
                    if (ext != "aqua") Left("File must be with 'aqua' extension")
                    else Right(p)
                  } else
                    Right(p)
                }
              else
                IO(Left(s"There is no path '${p.toString}'"))
            }
            // TODO: make it with correct effects
            .unsafeRunSync()
        }.toEither.left.map(t => s"An error occurred on imports reading: ${t.getMessage}").flatten)
        .toValidatedNel
  }

  def inputOpts(implicit runtime: unsafe.IORuntime): Opts[Path] =
    Opts
      .option[String](
        "input",
        "Path to an aqua file or an input directory that contains your .aqua files",
        "i"
      )
      .mapValidated(checkPath)

  def outputOpts(implicit runtime: unsafe.IORuntime): Opts[Path] =
    Opts.option[String]("output", "Path to the output directory", "o").mapValidated(checkPath)

  def importOpts(implicit runtime: unsafe.IORuntime): Opts[List[Path]] =
    Opts
      .options[String]("import", "Path to the directory to import from", "m")
      .mapValidated { ps =>
        val checked = ps
          .map(pStr => {
            Validated.catchNonFatal {
              val p = Path(pStr)
              (for {
                exists <- Files[IO].exists(p)
                isDir <- Files[IO].isDirectory(p)
              } yield {
                if (exists && isDir) Right(p)
                else Left(s"There is no path ${p.toString} or it is not a directory")
              })
                // TODO: make it with correct effects
                .unsafeRunSync()
            }
          })
          .toList
        checked.map {
          case Validated.Valid(pE) =>
            pE match {
              case Right(p) =>
                Validated.Valid(p)
              case Left(e) =>
                Validated.Invalid(e)
            }
          case Validated.Invalid(e) =>
            Validated.Invalid(s"Error occurred on imports reading: ${e.getMessage}")
        }.traverse {
          case Valid(a) => Validated.validNel(a)
          case Invalid(e) => Validated.invalidNel(e)
        }
      }
      .withDefault(List.empty)

  def constantOpts[F[_]: LiftParser: Comonad]: Opts[List[GenerationConfig.Const]] =
    Opts
      .options[String]("const", "Constant that will be used in an aqua code", "c")
      .mapValidated { strs =>
        val parsed = strs.map(s => ConstantExpr.onlyLiteral.parseAll(s))

        val errors = parsed.collect { case Left(er) =>
          er
        }

        NonEmptyList
          .fromList(errors)
          .fold(
            Validated.validNel[String, List[GenerationConfig.Const]](parsed.collect {
              case Right(v) =>
                GenerationConfig.Const(v._1.value, LiteralModel(v._2.value, v._2.ts))
            })
          ) { errors =>
            Validated.invalid(errors.map(_.toString))
          }
      }
      .withDefault(List.empty)

  val compileToAir: Opts[Boolean] =
    Opts
      .flag("air", "Generate .air file instead of typescript", "a")
      .map(_ => true)
      .withDefault(false)

  val compileToJs: Opts[Boolean] =
    Opts
      .flag("js", "Generate .js file instead of typescript")
      .map(_ => true)
      .withDefault(false)

  val noRelay: Opts[Boolean] =
    Opts
      .flag("no-relay", "Do not generate a pass through the relay node")
      .map(_ => true)
      .withDefault(false)

  val noXorWrapper: Opts[Boolean] =
    Opts
      .flag("no-xor", "Do not generate a wrapper that catches and displays errors")
      .map(_ => true)
      .withDefault(false)

  lazy val versionStr: String =
    Option(getClass.getPackage.getImplementationVersion).filter(_.nonEmpty).getOrElse("no version")

  def versionAndExit[F[_]: Console: Functor]: F[ExitCode] = Console[F]
    .println(versionStr)
    .as(ExitCode.Success)

  def helpAndExit[F[_]: Console: Functor]: F[ExitCode] = Console[F]
    .println(help)
    .as(ExitCode.Success)

  def wrapWithOption[A](opt: Opts[A]): Opts[Option[A]] =
    opt.map(v => Some(v)).withDefault(None)
}
