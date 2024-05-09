package aqua.parser.head

import aqua.AquaSpec
import aqua.parser.expr.func.ServiceIdExpr
import aqua.parser.lexer.{LiteralToken, Token}
import aqua.parser.lift.LiftParser.given
import aqua.types.LiteralType

import cats.Id
import cats.data.NonEmptyList
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ImportFromSpec extends AnyFlatSpec with Matchers with AquaSpec {
  import AquaSpec.*

  "import from" should "be parsed" in {
    ImportFromExpr.p
      .parseAll("import MyModule from \"file.aqua\"")
      .value
      .mapK(spanToId) should be(
      ImportFromExpr(
        NonEmptyList.one(toQNameAs("MyModule", None)),
        toStr("file.aqua")
      )
    )

    ImportFromExpr.p
      .parseAll("""import MyModule, func as fn from "file.aqua"""")
      .value
      .mapK(spanToId) should be(
      ImportFromExpr(
        NonEmptyList.of(
          toQNameAs("MyModule", None),
          toQNameAs("func", Some("fn"))
        ),
        toStr("file.aqua")
      )
    )
  }

}
