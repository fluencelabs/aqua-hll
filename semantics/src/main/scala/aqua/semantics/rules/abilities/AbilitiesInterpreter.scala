package aqua.semantics.rules.abilities

import aqua.parser.lexer.{Name, NamedTypeToken, Token, ValueToken}
import aqua.raw.value.ValueRaw
import aqua.raw.{RawContext, ServiceRaw}
import aqua.semantics.Levenshtein
import aqua.semantics.rules.errors.ReportErrors
import aqua.semantics.rules.locations.LocationsAlgebra
import aqua.semantics.rules.{abilities, StackInterpreter}
import aqua.types.ArrowType

import cats.data.{NonEmptyMap, State}
import cats.syntax.functor.*
import cats.syntax.foldable.*
import cats.syntax.traverse.*
import cats.syntax.applicative.*
import monocle.Lens
import monocle.macros.GenLens

class AbilitiesInterpreter[S[_], X](using
  lens: Lens[X, AbilitiesState[S]],
  error: ReportErrors[S, X],
  locations: LocationsAlgebra[S, State[X, *]]
) extends AbilitiesAlgebra[S, State[X, *]] {

  type SX[A] = State[X, A]

  private val stackInt = new StackInterpreter[S, X, AbilitiesState[S], AbilitiesState.Frame[S]](
    GenLens[AbilitiesState[S]](_.stack)
  )

  import stackInt.{getState, mapStackHead, mapStackHeadM, modify, report}

  override def defineService(
    name: NamedTypeToken[S],
    arrows: NonEmptyMap[String, (Name[S], ArrowType)],
    defaultId: Option[ValueRaw]
  ): SX[Boolean] =
    getService(name.value).flatMap {
      case Some(_) =>
        getState
          .map(_.definitions.get(name.value).exists(_ == name))
          .flatMap(exists =>
            report(
              name,
              "Service with this name was already defined"
            ).whenA(!exists)
          )
          .as(false)
      case None =>
        for {
          _ <- arrows.toNel.traverse_ { case (_, (n, arr)) =>
            report(n, "Service functions cannot have multiple results")
              .whenA(arr.codomain.length > 1)
          }
          _ <- modify(s =>
            s.copy(
              services = s.services
                .updated(name.value, ServiceRaw(name.value, arrows.map(_._2), defaultId)),
              definitions = s.definitions.updated(name.value, name)
            )
          )
          _ <- locations.addTokenWithFields(
            name.value,
            name,
            arrows.toNel.toList.map(t => t._1 -> t._2._1)
          )
        } yield true
    }

  // adds location from token to its definition
  private def addServiceArrowLocation(name: NamedTypeToken[S], arrow: Name[S]): SX[Unit] = {
    locations.pointTokenWithFieldLocation(name.value, name, arrow.value, arrow)
  }

  override def getArrow(name: NamedTypeToken[S], arrow: Name[S]): SX[Option[ArrowType]] =
    getService(name.value).map(_.map(_.arrows)).flatMap {
      case Some(arrows) =>
        arrows(arrow.value)
          .fold(
            report(
              arrow,
              Levenshtein.genMessage(
                s"Service is found, but arrow '${arrow.value}' isn't found in scope",
                arrow.value,
                arrows.value.keys.toNonEmptyList.toList
              )
            ).as(Option.empty[ArrowType])
          )(a => addServiceArrowLocation(name, arrow).as(Some(a)))
      case None =>
        getAbility(name.value).flatMap {
          case Some(abCtx) =>
            abCtx.funcs
              .get(arrow.value)
              .fold(
                report(
                  arrow,
                  Levenshtein.genMessage(
                    s"Ability is found, but arrow '${arrow.value}' isn't found in scope",
                    arrow.value,
                    abCtx.funcs.keys.toList
                  )
                ).as(Option.empty[ArrowType])
              ) { fn =>
                // TODO: add name and arrow separately
                // TODO: find tokens somewhere
                addServiceArrowLocation(name, arrow).as(Some(fn.arrow.`type`))
              }
          case None =>
            report(name, "Ability with this name is undefined").as(Option.empty[ArrowType])
        }
    }

  override def setServiceId(name: NamedTypeToken[S], id: ValueRaw): SX[Boolean] =
    getService(name.value).flatMap {
      case Some(_) =>
        mapStackHeadM(
          modify(st => st.copy(rootServiceIds = st.rootServiceIds.updated(name.value, id)))
            .as(true)
        )(h => (h.copy(serviceIds = h.serviceIds.updated(name.value, id)) -> true).pure)
      case None =>
        report(name, "Service with this name is not registered, can't set its ID").as(false)
    }

  override def getServiceId(name: NamedTypeToken[S]): SX[Either[Boolean, ValueRaw]] =
    getService(name.value).flatMap {
      case Some(_) =>
        getState.flatMap(st =>
          st.stack.collectFirstSome(_.serviceIds.get(name.value)) orElse
            st.rootServiceIds.get(name.value) orElse
            st.services.get(name.value).flatMap(_.defaultId) match {
            case None =>
              report(
                name,
                s"Service ID unresolved, use `${name.value} id` expression to set it"
              )
                .as(Left[Boolean, ValueRaw](false))

            case Some(v) => State.pure(Right(v))
          }
        )
      case None =>
        getAbility(name.value).flatMap {
          case Some(_) => State.pure(Left[Boolean, ValueRaw](true))
          case None =>
            report(name, "Ability with this name is undefined").as(
              Left[Boolean, ValueRaw](false)
            )
        }
    }

  override def beginScope(token: Token[S]): SX[Unit] =
    stackInt.beginScope(AbilitiesState.Frame[S](token))

  override def endScope(): SX[Unit] = stackInt.endScope

  private def getService(name: String): SX[Option[ServiceRaw]] =
    getState.map(_.services.get(name))

  private def getAbility(name: String): SX[Option[RawContext]] =
    getState.map(_.abilities.get(name))
}
