package aqua.semantics.expr

import aqua.generator.DataView
import aqua.model.{CoalgebraModel, Model, ServiceModel}
import aqua.parser.expr.CoalgebraExpr
import aqua.semantics.{ArrowType, Prog, Type}
import aqua.semantics.rules.ValuesAlgebra
import aqua.semantics.rules.abilities.AbilitiesAlgebra
import aqua.semantics.rules.names.NamesAlgebra
import aqua.semantics.rules.types.TypesAlgebra
import cats.free.Free
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.apply._

class CoalgebraSem[F[_]](val expr: CoalgebraExpr[F]) extends AnyVal {

  import expr._

  private def freeUnit[Alg[_]]: Free[Alg, Unit] = Free.pure[Alg, Unit](())

  private def checkArgsRes[Alg[_]](
    at: ArrowType
  )(implicit N: NamesAlgebra[F, Alg], V: ValuesAlgebra[F, Alg]): Free[Alg, List[(DataView, Type)]] =
    V.checkArguments(at, args) >> variable
      .fold(freeUnit[Alg])(exportVar =>
        at.res.fold(
          // TODO: error! we're trying to export variable, but function has no export type
          freeUnit[Alg]
        )(resType => N.define(exportVar, resType).void)
      ) >> args.foldLeft(Free.pure[Alg, List[(DataView, Type)]](Nil)) { case (acc, v) =>
      (acc, V.resolveType(v)).mapN((a, b) => a ++ b.map(ValuesAlgebra.valueToData(v) -> _))
    }

  private def toModel[Alg[_]](implicit
    N: NamesAlgebra[F, Alg],
    A: AbilitiesAlgebra[F, Alg],
    T: TypesAlgebra[F, Alg],
    V: ValuesAlgebra[F, Alg]
  ): Free[Alg, Option[CoalgebraModel]] =
    ability match {
      case Some(ab) =>
        (A.getArrow(ab, funcName), A.getServiceId(ab)).mapN {
          case (Some(at), Some(sid)) =>
            Option(at -> sid) // Here we assume that Ability is a Service that must be resolved
          case _ => None
        }.flatMap(_.fold(Free.pure[Alg, Option[CoalgebraModel]](None)) { case (arrowType, serviceId) =>
          checkArgsRes(arrowType)
            .map(argsResolved =>
              CoalgebraModel(
                ability = Some(ServiceModel(ab.value, ValuesAlgebra.valueToData(serviceId))),
                funcName = funcName.value,
                args = argsResolved,
                exportTo = variable.map(_.value)
              )
            )
            .map(Option(_))
        })
      case None =>
        N.readArrow(funcName)
          .flatMap(_.fold(Free.pure[Alg, Option[CoalgebraModel]](None)) { arrowType =>
            checkArgsRes(arrowType)
              .map(argsResolved =>
                CoalgebraModel(
                  ability = None,
                  funcName = funcName.value,
                  args = argsResolved,
                  exportTo = variable.map(_.value)
                )
              )
              .map(Option(_))
          })
    }

  def program[Alg[_]](implicit
    N: NamesAlgebra[F, Alg],
    A: AbilitiesAlgebra[F, Alg],
    T: TypesAlgebra[F, Alg],
    V: ValuesAlgebra[F, Alg]
  ): Prog[Alg, Model] =
    toModel[Alg].map(_.getOrElse(Model.error))

}
