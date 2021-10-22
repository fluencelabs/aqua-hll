package aqua.parser.expr.func

import aqua.parser.Expr
import aqua.parser.expr.func.CallArrowExpr
import aqua.parser.lexer.Token.*
import aqua.parser.lexer.{Ability, Name, Value}
import aqua.parser.lift.LiftParser
import cats.data.NonEmptyList
import cats.parse.{Parser as P, Parser0 as P0}
import cats.{Comonad, ~>}
import aqua.parser.lift.Span
import aqua.parser.lift.Span.{P0ToSpan, PToSpan}

case class CallArrowExpr[F[_]](
  variables: List[Name[F]],
  ability: Option[Ability[F]],
  funcName: Name[F],
  args: List[Value[F]]
) extends Expr[F](CallArrowExpr, funcName) {

  def mapK[K[_]: Comonad](fk: F ~> K): CallArrowExpr[K] =
    copy(
      variables.map(_.mapK(fk)),
      ability.map(_.mapK(fk)),
      funcName.mapK(fk),
      args.map(_.mapK(fk))
    )
}

object CallArrowExpr extends Expr.Leaf {

  val ability: P0[Option[Ability[Span.F]]] = (Ability.dotted <* `.`).?
  val functionCallWithArgs = Name.p
    ~ comma0(Value.`value`.surroundedBy(`/s*`)).between(`(` <* `/s*`, `/s*` *> `)`)
  val funcCall = ability.with1 ~ functionCallWithArgs
  
  val funcOnly = funcCall.map {
    case (ab, (name, args)) =>
      CallArrowExpr(Nil, ab, name, args)
  }

  override val p: P[CallArrowExpr[Span.F]] = {
    val variables: P0[Option[NonEmptyList[Name[Span.F]]]] = (comma(Name.p) <* ` <- `).backtrack.?

    (variables.with1 ~ funcCall.withContext("Only results of a function call can be written to a stream")
      ).map {
      case (variables, (ability, (funcName, args))) =>
        CallArrowExpr(variables.toList.flatMap(_.toList), ability, funcName, args)
    }
  }

}
