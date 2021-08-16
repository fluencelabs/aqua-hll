package aqua.parser.head

import aqua.parser.lexer.Token._
import aqua.parser.lexer.{Literal, Value}
import aqua.parser.lift.LiftParser
import cats.Comonad
import cats.parse.Parser

case class ImportExpr[F[_]](filename: Literal[F]) extends FilenameExpr[F]

object ImportExpr extends HeaderExpr.Leaf {

  override def p[F[_]: LiftParser: Comonad]: Parser[HeaderExpr[F]] =
    `import` *> ` ` *> Value.string[F].map(ImportExpr(_))
}
