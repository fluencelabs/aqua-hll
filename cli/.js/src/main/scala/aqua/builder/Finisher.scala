package aqua.builder

import aqua.backend.{ArgDefinition, ServiceDef, ServiceFunctionDef, TypeDefinition, VoidType}
import aqua.io.OutputPrinter
import aqua.js.{CallJsFunction, FluencePeer, ServiceHandler}
import aqua.model.{LiteralModel, VarModel}
import aqua.raw.ops.{Call, CallServiceTag}
import aqua.raw.value.LiteralRaw
import cats.data.NonEmptyList

import scala.concurrent.Promise
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.Dynamic

// Will finish promise on service call
abstract class Finisher private (
  serviceId: String,
  functions: NonEmptyList[AquaFunction],
  val promise: Promise[Unit]
) extends Service(serviceId, functions) {

  def callTag(): CallServiceTag
}

object Finisher {

  private def finishFunction(funcName: String, promise: Promise[Unit]) = new AquaFunction {
    def fnName: String = funcName

    def handler: ServiceHandler = _ => {
      promise.success(())
      js.Promise.resolve(Service.emptyObject)
    }
    def argDefinitions: List[ArgDefinition] = Nil
    def returnType: TypeDefinition = VoidType
  }

  def apply(servId: String, fnName: String): Finisher = {
    val promise = Promise[Unit]()
    val funcs = NonEmptyList.one(finishFunction(fnName, promise))
    new Finisher(servId, funcs, promise) {
      def callTag(): CallServiceTag = {
        CallServiceTag(
          LiteralRaw.quote(servId),
          fnName,
          Call(Nil, Nil)
        )
      }
    }
  }
}
