package aqua.model.transform

import aqua.model.{ValueModel, VarModel}
import aqua.model.func.{ArgDef, ArgsCall, ArgsDef, Call, FuncCallable}
import aqua.model.func.body.{FuncOp, FuncOps}
import aqua.types.ArrowType
import cats.Eval
import cats.syntax.apply._
import cats.syntax.functor._
import wvlet.log.Logger

case class ResolveFunc(
  transform: FuncOp => FuncOp,
  callback: (String, Call) => FuncOp,
  respFuncName: String,
  wrapCallableName: String = "funcAround",
  arrowCallbackPrefix: String = "init_peer_callable_"
) {

  private val logger = Logger.of[ResolveFunc]
  import logger._

  def returnCallback(retModel: ValueModel): FuncOp =
    callback(
      respFuncName,
      Call(
        retModel :: Nil,
        None
      )
    )

  def arrowToCallback(name: String, arrowType: ArrowType): FuncCallable = {
    val (args, call, ret) = ArgsCall.arrowToArgsCallRet(arrowType)
    FuncCallable(
      arrowCallbackPrefix + name,
      callback(name, call),
      args,
      (ret.map(_.model), arrowType.res).mapN(_ -> _),
      Map.empty,
      Map.empty
    )
  }

  def wrap(func: FuncCallable): FuncCallable =
    FuncCallable(
      wrapCallableName,
      transform(
        FuncOps.seq(
          FuncOps
            .callArrow(
              func.funcName,
              Call(
                func.args.toCallArgs,
                func.ret.map(rmv => Call.Export("-return-", rmv._1.lastType))
              )
            ) ::
            func.ret
              .map(_._1)
              .map(rmv => VarModel("-return-", rmv.lastType))
              .map(returnCallback)
              .toList: _*
        )
      ),
      ArgsDef(ArgDef.Arrow(func.funcName, func.arrowType) :: Nil),
      None,
      func.args.arrowArgs.map { case ArgDef.Arrow(argName, arrowType) =>
        argName -> arrowToCallback(argName, arrowType)
      }.toList.toMap,
      Map.empty
    )

  def resolve(
    func: FuncCallable,
    funcArgName: String = "_func"
  ): Eval[FuncOp] =
    wrap(func)
      .resolve(
        Call(VarModel(funcArgName, func.arrowType) :: Nil, None),
        Map(funcArgName -> func),
        Set.empty
      )
      .map(_._1)
}
