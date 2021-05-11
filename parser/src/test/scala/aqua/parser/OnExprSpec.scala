package aqua.parser

import aqua.AquaSpec
import aqua.parser.expr.OnExpr
import cats.Id
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class OnExprSpec extends AnyFlatSpec with Matchers with AquaSpec {
  import AquaSpec._

  "on" should "be parsed" in {
    parseOn("on peer") should be(
      OnExpr[Id](toVar("peer"), Nil, None)
    )

    parseOn("on peer.id") should be(
      OnExpr[Id](toVarLambda("peer", List("id")), Nil, None)
    )

    parseOn("on \"peer\"") should be(
      OnExpr[Id](toStr("peer"), Nil, None)
    )

    parseOn("on 1") should be(
      OnExpr[Id](toNumber(1), Nil, None)
    )

    parseOn("on \"asd\" via \"fre\" via \"fre2\"") should be(
      OnExpr[Id](toStr("asd"), List(toStr("fre"), toStr("fre2")), None)
    )
  }
}
