package aqua.builder

import aqua.backend.{
  ArgDefinition,
  PrimitiveType,
  ServiceDef,
  ServiceFunctionDef,
  TypeDefinition,
  VoidType
}
import aqua.io.OutputPrinter
import aqua.js.{CallJsFunction, FluencePeer, ServiceHandler}
import cats.data.NonEmptyList

import scala.scalajs.js
import scala.scalajs.js.JSON

private case class Console(serviceId: String, functions: NonEmptyList[AquaFunction])
    extends Service(serviceId, functions)

object Console {

  private def printFunction(funcName: String) = new AquaFunction {
    override def fnName: String = funcName

    def handler: ServiceHandler = { varArgs =>
      OutputPrinter.print(JSON.stringify(varArgs(0), space = 2))
      js.Promise.resolve(ServiceFunction.emptyObject)
    }
    def argDefinitions: List[ArgDefinition] = ArgDefinition("str", PrimitiveType) :: Nil
    def returnType: TypeDefinition = VoidType
  }

  val PrintName = "print"

  def apply(serviceId: String = "run-console"): Console = {

    Console(serviceId, NonEmptyList.one(printFunction(PrintName)))
  }
}
