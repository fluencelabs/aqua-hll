package aqua.parser.head

import aqua.parser.lexer.Token._
import aqua.parser.lexer.{Ability, Name}
import aqua.parser.lift.LiftParser
import cats.Comonad
import cats.data.NonEmptyList
import cats.parse.Parser as P

trait FromExpr[F[_]] {
  def imports: NonEmptyList[FromExpr.NameOrAbAs[F]]
}

object FromExpr {
  type NameOrAbAs[F[_]] = Either[Name.As[F], Ability.As[F]]

  def nameOrAbAs[F[_]: LiftParser: Comonad]: P[NameOrAbAs[F]] =
    Name.nameAs[F].map(Left(_)) | Ability.abAs[F].map(Right(_))

  def importFrom[F[_]: LiftParser: Comonad]: P[NonEmptyList[NameOrAbAs[F]]] =
      comma[NameOrAbAs[F]](nameOrAbAs[F]) <* ` ` <* `from`
}