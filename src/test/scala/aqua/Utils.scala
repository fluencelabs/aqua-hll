package aqua

import aqua.parser.expr.{AbilityIdExpr, AliasExpr, ArrowTypeExpr, CoalgebraExpr, DataStructExpr, FuncExpr, OnExpr}
import aqua.parser.lexer.{
  Ability,
  Arg,
  ArrowTypeToken,
  BasicTypeToken,
  CustomTypeToken,
  DataTypeToken,
  IntoField,
  Literal,
  Name,
  TypeToken,
  VarLambda
}
import cats.Id
import aqua.parser.lift.LiftParser.Implicits.idLiftParser
import aqua.semantics.{LiteralType, ScalarType}
import org.scalatest.EitherValues

import scala.language.implicitConversions

object Utils {
  implicit def toAb(str: String): Ability[Id] = Ability[Id](str)

  implicit def toName(str: String): Name[Id] = Name[Id](str)

  implicit def toFields(fields: List[String]): List[IntoField[Id]] = fields.map(f => IntoField[Id](f))

  implicit def toVar(name: String, fields: List[String]): VarLambda[Id] = VarLambda[Id](toName(name), toFields(fields))
  implicit def toLiteral(name: String, t: LiteralType): Literal[Id] = Literal[Id](name, t)

  implicit def toCustomType(str: String): CustomTypeToken[Id] = CustomTypeToken[Id](str)

  implicit def toArrowType(args: List[DataTypeToken[Id]], res: Option[DataTypeToken[Id]]): ArrowTypeToken[Id] =
    ArrowTypeToken[Id]((), args, res)

  implicit def toCustomArg(str: String, customType: String): Arg[Id] = Arg[Id](str, toCustomType(customType))

  implicit def toArg(str: String, typeToken: TypeToken[Id]): Arg[Id] = Arg[Id](str, typeToken)

  implicit def scToBt(sc: ScalarType): BasicTypeToken[Id] = BasicTypeToken[Id](sc)
}

trait Utils extends EitherValues {

  def parseExpr(str: String): CoalgebraExpr[Id] =
    CoalgebraExpr.p[Id].parseAll(str).value

  def parseAbId(str: String): AbilityIdExpr[Id] =
    AbilityIdExpr.p[Id].parseAll(str).value

  def parseOn(str: String): OnExpr[Id] =
    OnExpr.p[Id].parseAll(str).value

  def parseAlias(str: String): AliasExpr[Id] =
    AliasExpr.p[Id].parseAll(str).value

  def parseDataStruct(str: String): DataStructExpr[Id] =
    DataStructExpr.p[Id].parseAll(str).value

  def parseArrow(str: String): ArrowTypeExpr[Id] =
    ArrowTypeExpr.p[Id].parseAll(str).value

  def funcExpr(str: String): FuncExpr[Id] = FuncExpr.p[Id].parseAll(str).value
}
