package aqua.ast.expr

import aqua.ast.{Expr, Gen, Prog}
import aqua.ast.algebra.abilities.AbilitiesAlgebra
import aqua.ast.algebra.types.TypesAlgebra
import aqua.parser.lexer.Token._
import aqua.parser.lexer.{ArrowTypeToken, Name}
import aqua.parser.lift.LiftParser
import cats.Comonad
import cats.free.Free
import cats.parse.Parser
import cats.syntax.functor._

case class ArrowTypeExpr[F[_]](name: Name[F], `type`: ArrowTypeToken[F]) extends Expr[F] {

  def program[Alg[_]](implicit T: TypesAlgebra[F, Alg], A: AbilitiesAlgebra[F, Alg]): Prog[Alg, Gen] =
    T.resolveArrowDef(`type`).flatMap {
      case Some(t) => A.defineArrow(name, t) as Gen(s"Arrow $name defined")
      case None => Free.pure(Gen(s"Arrow $name can't be defined"))
    }

}

object ArrowTypeExpr extends Expr.Leaf {

  override def p[F[_]: LiftParser: Comonad]: Parser[Expr[F]] =
    ((Name.p[F] <* ` : `) ~ ArrowTypeToken.`arrowdef`[F]).map {
      case (name, t) => ArrowTypeExpr(name, t)
    }
}
