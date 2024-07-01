/*
 * Copyright (C) 2024  Fluence DAO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package aqua.compiler

import aqua.model.*
import aqua.model.transform.{ModelBuilder, TransformConfig}
import aqua.res.*
import aqua.types.*

import org.scalatest.Inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AquaModulesSpec extends CompilerSpec {
  import ModelBuilder.*

  // Importing empty module caused internal compiler error once
  it should "sucessfully import empty module" in {
    val module = s"""aqua Module
                    |""".stripMargin
    val main = s"""aqua Main
                  |
                  |use "module"
                  |
                  |export main
                  |
                  |func main() -> i32:
                  |  <- 42
                  |""".stripMargin

    val src = Map("main.aqua" -> main)
    val imports = Map("module.aqua" -> module)

    val transformCfg = TransformConfig(relayVarName = None)

    insideRes(src, imports, transformCfg)(
      "main"
    ) { case main :: _ =>
      val ap = CallModel.Export("literal_ap", LiteralType.unsigned)
      val props = ap.copy(name = "literal_props")
      val expected = XorRes.wrap(
        respCall(transformCfg, LiteralModel.number(42), initPeer),
        errorCall(transformCfg, 0, initPeer)
      )

      main.body.equalsOrShowDiff(expected) should be(true)
    }

  }

  it should "merge modules on redeclaration" in {
    def test(subA: String, subB: String) = {
      val moduleA = s"""aqua Module.$subA declares fA
                       |func fA() -> i32:
                       |  <- 42
                       |""".stripMargin
      val moduleB = s"""aqua Module.$subB  declares fB
                       |func fB() -> i32:
                       |  <- 24
                       |""".stripMargin
      val merged = s"""aqua Merged declares Module
                      |use "moduleA"
                      |use "moduleB"
      """.stripMargin
      val main = s"""aqua Main
                    |
                    |use "merged"
                    |
                    |export main
                    |
                    |func main() -> i32:
                    |  <- Merged.Module.$subA.fA() + Merged.Module.$subB.fB()
                    |""".stripMargin

      val src = Map("main.aqua" -> main)
      val imports = Map(
        "moduleA.aqua" -> moduleA,
        "moduleB.aqua" -> moduleB,
        "merged.aqua" -> merged
      )

      val transformCfg = TransformConfig(relayVarName = None)

      insideRes(src, imports, transformCfg)(
        "main"
      ) { case main :: _ =>
        val ap = CallModel.Export("literal_ap", LiteralType.unsigned)
        val props = ap.copy(name = "literal_props")
        val expected = XorRes.wrap(
          respCall(transformCfg, LiteralModel.number(42 + 24), initPeer),
          errorCall(transformCfg, 0, initPeer)
        )

        main.body.equalsOrShowDiff(expected) should be(true)
      }
    }

    test("A", "B")
    test("Lib.A", "Lib.B")
    test("Lib.Sub", "Lib.Sub.Path")
  }

}
