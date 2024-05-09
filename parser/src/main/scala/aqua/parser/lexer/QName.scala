package aqua.parser.lexer

import aqua.helpers.data.PName
import aqua.parser.lexer.Token.*
import aqua.parser.lift.LiftParser
import aqua.parser.lift.LiftParser.*
import aqua.parser.lift.Span
import aqua.parser.lift.Span.{given, *}

import cats.Comonad
import cats.arrow.FunctionK
import cats.data.NonEmptyList
import cats.parse.{Parser => P}
import cats.syntax.comonad.*
import cats.syntax.functor.*
import cats.~>

/**
 * Qualified name. Name with parts separated by `.`
 * e.g. `Some.Imported.Module.foo`
 *
 * @param name Name as a whole
 * @param parts Parts of the name
 */
final case class QName[F[_]: Comonad](
  name: F[String],
  parts: NonEmptyList[F[String]]
) extends Token[F] {

  def value: String = name.extract
  override def as[T](v: T): F[T] = name.as(v)

  override def mapK[K[_]: Comonad](fk: FunctionK[F, K]): QName[K] =
    copy(fk(name), parts.map(p => fk(p)))

  def toPName: PName = PName(parts.map(_.extract))
}

object QName {

  val p: P[QName[Span.S]] =
    anyName.lift
      .repSep(`.`)
      .withString
      .lift
      .map(span => {
        val name = span.fmap { case (_, name) => name }
        val parts = span.fmap { case (parts, _) => parts }.extract
        QName(name, parts)
      })

  final case class As[F[_]: Comonad](
    name: QName[F],
    rename: Option[QName[F]]
  ) {

    def mapK[K[_]: Comonad](fk: F ~> K): As[K] =
      copy(name = name.mapK(fk), rename = rename.map(_.mapK(fk)))
  }

  val as: P[As[Span.S]] =
    asOpt(p).map(As.apply)
}
