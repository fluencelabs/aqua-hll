package aqua.files

import aqua.AquaIO
import aqua.compiler.{AquaCompiled, AquaSources}
import aqua.io.AquaFileError
import cats.Monad
import cats.data.{Chain, NonEmptyChain, Validated, ValidatedNec}
import cats.implicits.catsSyntaxApplicativeId
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._

import java.nio.file.Path

class AquaFileSources[F[_]: AquaIO: Monad](sourcesPath: Path, importFrom: List[Path])
    extends AquaSources[F, AquaFileError, FileModuleId] {
  private val filesIO = implicitly[AquaIO[F]]

  override def sources: F[ValidatedNec[AquaFileError, Chain[(FileModuleId, String)]]] =
    filesIO.listAqua(sourcesPath).flatMap {
      case Validated.Valid(files) =>
        files
          .map(f =>
            filesIO
              .readFile(f)
              .value
              .map[ValidatedNec[AquaFileError, Chain[(FileModuleId, String)]]] {
                case Left(err) =>
                  println(err)
                  Validated.invalidNec(err)
                case Right(content) =>
                  println(content)
                  Validated.validNec(Chain.one(FileModuleId(f) -> content))
              }
          )
          .traverse(identity)
          .map(
            _.foldLeft[ValidatedNec[AquaFileError, Chain[(FileModuleId, String)]]](
              Validated.validNec(Chain.nil)
            )(_ combine _)
          )
      case err @ Validated.Invalid(e) =>
        println(e)

        Validated.invalidNec[AquaFileError, Chain[(FileModuleId, String)]](e.head).pure[F]
    }

  override def resolve(
    from: FileModuleId,
    imp: String
  ): F[ValidatedNec[AquaFileError, FileModuleId]] =
    filesIO
      .resolve(from.file, importFrom)
      .bimap(NonEmptyChain.one, FileModuleId(_))
      .value
      .map(Validated.fromEither)

  override def load(file: FileModuleId): F[ValidatedNec[AquaFileError, String]] =
    filesIO.readFile(file.file).leftMap(NonEmptyChain.one).value.map(Validated.fromEither)

  def write(
    targetPath: Path
  )(ac: AquaCompiled[FileModuleId]): F[Validated[AquaFileError, String]] = {
    // TODO: this does not respect source subfolders
    val target = targetPath.resolve(
      ac.sourceId.file.getFileName.toString.stripSuffix(".aqua") + ac.compiled.suffix
    )
    println(target)
    filesIO
      .writeFile(
        target,
        ac.compiled.content
      )
      .as(target.toString)
      .value
      .map(Validated.fromEither)
  }
}
