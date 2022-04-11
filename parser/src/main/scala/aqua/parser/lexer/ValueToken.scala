package aqua.parser.lexer

import aqua.parser.Expr
import aqua.parser.head.FilenameExpr
import aqua.parser.lexer.Token.*
import aqua.parser.lift.LiftParser
import aqua.parser.lift.LiftParser.*
import aqua.types.LiteralType
import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import cats.syntax.comonad.*
import cats.syntax.functor.*
import cats.{~>, Comonad, Functor}
import cats.data.NonEmptyList
import aqua.parser.lift.Span
import aqua.parser.lift.Span.{P0ToSpan, PToSpan, S}

sealed trait ValueToken[F[_]] extends Token[F] {
  def mapK[K[_]: Comonad](fk: F ~> K): ValueToken[K]
}

case class VarToken[F[_]](name: Name[F], lambda: List[LambdaOp[F]] = Nil) extends ValueToken[F] {
  override def as[T](v: T): F[T] = name.as(v)

  def mapK[K[_]: Comonad](fk: F ~> K): VarToken[K] = copy(name.mapK(fk), lambda.map(_.mapK(fk)))
}

case class LiteralToken[F[_]: Comonad](valueToken: F[String], ts: LiteralType)
    extends ValueToken[F] {
  override def as[T](v: T): F[T] = valueToken.as(v)

  def mapK[K[_]: Comonad](fk: F ~> K): LiteralToken[K] = copy(fk(valueToken), ts)

  def value: String = valueToken.extract

  override def toString: String = s"$value"
}

case class CollectionToken[F[_]: Comonad](
  point: F[CollectionToken.Mode],
  values: List[ValueToken[F]]
) extends ValueToken[F] {

  override def mapK[K[_]: Comonad](fk: F ~> K): ValueToken[K] =
    copy(fk(point), values.map(_.mapK(fk)))

  override def as[T](v: T): F[T] = point.as(v)

  def mode: CollectionToken.Mode = point.extract
}

object CollectionToken {

  enum Mode:
    case StreamMode, OptionMode, ArrayMode

  val collection: P[CollectionToken[Span.S]] =
    ((
      `*[`.as[Mode](Mode.StreamMode) |
        `?[`.as[Mode](Mode.OptionMode) |
        `[`.as[Mode](Mode.ArrayMode)
    ).lift ~ (P
      .defer(ValueToken.`_value`)
      .repSep0(`,`) <* `]`)).map { case (mode, vals) =>
      CollectionToken(mode, vals)
    }
}

case class CallArrowToken[F[_]: Comonad](
  ability: Option[Ability[F]],
  funcName: Name[F],
  args: List[ValueToken[F]]
) extends ValueToken[F] {

  override def mapK[K[_]: Comonad](fk: F ~> K): CallArrowToken[K] =
    copy(ability.map(_.mapK(fk)), funcName.mapK(fk), args.map(_.mapK(fk)))

  override def as[T](v: T): F[T] = funcName.as(v)
}

object CallArrowToken {

  val callArrow: P[CallArrowToken[Span.S]] =
    ((Ability.dotted <* `.`).?.with1 ~
      (Name.p
        ~ comma0(ValueToken.`_value`.surroundedBy(`/s*`)).between(`(` <* `/s*`, `/s*` *> `)`))
        .withContext(
          "Missing braces '()' after the function call"
        )).map { case (ab, (fn, args)) =>
      CallArrowToken(ab, fn, args)
    }
}

// Two values as operands, with an infix between them
case class InfixToken[F[_]: Comonad](
  left: ValueToken[F],
  right: ValueToken[F],
  infix: F[InfixToken.Op]
) extends ValueToken[F] {

  val op: InfixToken.Op = infix.extract

  override def mapK[K[_]: Comonad](fk: F ~> K): ValueToken[K] =
    copy(left.mapK(fk), right.mapK(fk), fk(infix))

  override def as[T](v: T): F[T] = infix.as(v)

  override def toString: String = s"($op $left $right)"
}

object InfixToken {

  enum Op:
    case Pow, Mul, Div, Rem, Add, Sub, Gt, Gte, Lt, Lte

  // TODO: convert it to a map or something and use it in the parser to avoid repetitions?
  val ops: List[P[Span.S[Op]]] =
    List(
      `**`.as(Op.Pow),
      `*`.as(Op.Mul),
      `/`.as(Op.Div),
      `%`.as(Op.Rem),
      `-`.as(Op.Sub),
      `+`.as(Op.Add),
      `>`.as(Op.Gt),
      `>=`.as(Op.Gte),
      `<`.as(Op.Lt),
      `<=`.as(Op.Lte)
    ).map(_.lift)

  // Parse left-associative operations `basic (OP basic)*`.
  // We use this form to avoid left recursion.
  private def infixParserLeft(basic: P[ValueToken[S]], ops: List[P[Op]]) =
    (basic ~ (P.oneOf(ops.map(_.lift)).surroundedBy(`/s*`) ~ basic).backtrack.rep0).map {
      case (vt, list) =>
        list.foldLeft(vt) { case (acc, (op, value)) =>
          InfixToken(acc, value, op)
        }
    }

  // Parse right-associative operations: `basic OP recursive`.
  private def infixParserRight(basic: P[ValueToken[S]], ops: List[P[Op]]): P[ValueToken[S]] =
    P.recursive { recurse =>
      (basic ~ (P.oneOf(ops.map(_.lift)).surroundedBy(`/s*`) ~ recurse).backtrack.?).map {
        case (vt, Some((op, recVt))) =>
          InfixToken(vt, recVt, op)
        case (vt, None) =>
          vt
      }
    }

  // Parse non-associative operations: `basic OP basic`.
  private def infixParserNone(basic: P[ValueToken[S]], ops: List[P[Op]]) =
    (basic ~ (P.oneOf(ops.map(_.lift)).surroundedBy(`/s*`) ~ basic).backtrack.?) map {
      case (leftVt, Some((op, rightVt))) =>
        InfixToken(leftVt, rightVt, op)
      case (vt, None) =>
        vt
    }

  val pow: P[ValueToken[Span.S]] =
    infixParserRight(ValueToken.atom, `**`.as(Op.Pow) :: Nil)

  val mult: P[ValueToken[Span.S]] =
    infixParserLeft(pow, `*`.as(Op.Mul) :: `/`.as(Op.Div) :: `%`.as(Op.Rem) :: Nil)

  val add: P[ValueToken[Span.S]] =
    infixParserLeft(mult, `+`.as(Op.Add) :: `-`.as(Op.Sub) :: Nil)

  val compare: P[ValueToken[Span.S]] =
    infixParserNone(
      add,
      `>`.as(Op.Gt) :: `>=`.as(Op.Gte) :: `<`.as(Op.Lt) :: `<=`.as(Op.Lte) :: Nil
    )

  /**
   * The math expression parser.
   *
   * Fist, the general idea. We'll consider only the expressions with operations `+`, `-`, and `*`, with bracket
   * support. This syntax would typically be defined as follows:
   *
   * mathExpr ->
   *   number
   *   | mathExpr + mathExpr
   *   | mathExpr - mathExpr
   *   | mathExpr * mathExpr
   *   | ( mathExpr )
   *
   * However, this grammar is ambiguous. For example, there are valid two parse trees for string `1 + 3 * 4`:
   *
   * 1)
   *      +
   *    /   \
   *   1    *
   *      /   \
   *     3     4
   * 2)
   *       *
   *     /   \
   *    +    4
   *  /   \
   * 1     3
   *
   * We will instead define the grammar in a way that only allows a single possible parse tree.
   * This parse tree will have the correct precedence and associativity of the operations built in.
   *
   * The intuition behind such grammar rules is as follows.
   * For example, 1 + 2 - 3 * 4 + 5 * (6 + 1) can be thought of as a string of the form
   *             `_ + _ - _____ + ___________`, where
   * the underscores denote products of one or more numbers or bracketed expressions.
   *
   * In other words, an expression of this form is *the sum of several products*. We can encode this, as
   * well as the fact that addition and subtraction are left-associative, as this rule:
   * addExpr
   *   -> addExpr ADD_OP multExpr
   *   | multExpr
   *
   * If we parse the string like this, then precedence of `+`, `-`, and `*` will work correctly out of the box,
   *
   * The grammar below expresses the operator precedence and associativity we expect from math expressions:
   *
   * -- Comparison is the entry point because it has the lowest priority.
   * mathExpr
   *   -> cmpExpr
   *
   * -- Comparison isn't an associative operation so it's not a recursive definition.
   * cmpExpr
   *   -> addExpr CMP_OP addExpr
   *   | addExpr
   *
   * -- Addition is a left associative operation, so it calls itself recursively on the left.
   * -- To avoid the left recursion problem this is implemented as `multExpr (ADD_OP multExpr)*`.
   * addExpr
   *   -> addExpr ADD_OP multExpr
   *   | multExpr
   *
   * -- Multiplication is also a left associative operation, so it calls itself recursively on the left.
   * -- To avoid the left recursion problem actually we employ the `expExpr (ADD_OP expExpr)*` form.
   * multExpr
   *   -> multExpr MULT_OP expExpr
   *   |  expExpr
   *
   * -- Exponentiation is a right associative operation, so it calls itself recursively on the right.
   * expExpr
   *   -> atom EXP_OP exprExpr
   *   | atom
   *
   * -- Atomic expression is an expression that can be parsed independently of what's going on around it.
   * -- For example, an expression in brackets will be parsed the same way regardless of what part of the
   * -- expression it's in.
   * atom
   *   -> number
   *   | literal
   *   | ...
   *   | ( mathExpr )
   */
  val mathExpr: P[ValueToken[Span.S]] = compare
}

object ValueToken {

  val varLambda: P[VarToken[Span.S]] =
    (Name.dotted ~ LambdaOp.ops.?).map { case (n, l) ⇒
      VarToken(n, l.fold[List[LambdaOp[Span.S]]](Nil)(_.toList))
    }

  val bool: P[LiteralToken[Span.S]] =
    P.oneOf(
      ("true" :: "false" :: Nil)
        .map(t ⇒ P.string(t).lift.map(fu => LiteralToken(fu.as(t), LiteralType.bool)))
    ) <* P.not(`anum_*`)

  val initPeerId: P[LiteralToken[Span.S]] =
    `%init_peer_id%`.string.lift.map(LiteralToken(_, LiteralType.string))

  val minus = P.char('-')
  val dot = P.char('.')

  val num: P[LiteralToken[Span.S]] =
    (minus.?.with1 ~ Numbers.nonNegativeIntString).lift.map(fu =>
      fu.extract match {
        case (Some(_), n) ⇒ LiteralToken(fu.as(s"-$n"), LiteralType.signed)
        case (None, n) ⇒ LiteralToken(fu.as(n), LiteralType.number)
      }
    )

  val float: P[LiteralToken[Span.S]] =
    (minus.?.with1 ~ (Numbers.nonNegativeIntString <* dot) ~ Numbers.nonNegativeIntString).string.lift
      .map(LiteralToken(_, LiteralType.float))

  val charsWhileQuotes = P.charsWhile0(_ != '"')

  // TODO make more sophisticated escaping/unescaping
  val string: P[LiteralToken[Span.S]] =
    (`"` *> charsWhileQuotes <* `"`).string.lift
      .map(LiteralToken(_, LiteralType.string))

  val literal: P[LiteralToken[Span.S]] =
    P.oneOf(bool.backtrack :: float.backtrack :: num.backtrack :: string :: Nil)

  def brackets(basic: P[ValueToken[Span.S]]): P[ValueToken[Span.S]] =
    basic.between(`(`, `)`).backtrack

  val atom: P[ValueToken[S]] = P.oneOf(
    literal.backtrack ::
      initPeerId.backtrack ::
      P.defer(
        CollectionToken.collection
      ) ::
      P.defer(CallArrowToken.callArrow).backtrack ::
      P.defer(brackets(InfixToken.mathExpr)) ::
      varLambda ::
      Nil
  )

  // One of entry points for parsing the whole math expression
  def `_value`: P[ValueToken[Span.S]] = {
    InfixToken.mathExpr
  }

  // One of entry points for parsing the whole math expression
  val `value`: P[ValueToken[Span.S]] =
    P.defer(`_value`)

}
