package aqua.parser.expr

import aqua.parser.Expr
import aqua.parser.lexer.Token
import aqua.parser.lexer.Token._
import aqua.parser.lift.LiftParser
import aqua.parser.lift.LiftParser._
import cats.Comonad
import cats.parse.Parser

case class ElseOtherwiseExpr[F[_]](point: Token[F]) extends Expr[F](ElseOtherwiseExpr, point)

object ElseOtherwiseExpr extends Expr.AndIndented {

  override def validChildren: List[Expr.Lexem] = ForExpr.validChildren

  override def p[F[_]: LiftParser: Comonad]: Parser[ElseOtherwiseExpr[F]] =
    (`else` | `otherwise`).lift.map(Token.lift[F, Unit](_)).map(ElseOtherwiseExpr(_))
}
