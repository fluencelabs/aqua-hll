package aqua.model.inline

import aqua.model.inline.state.{Arrows, Counter, Exports, Mangler}
import aqua.model.*
import aqua.model.inline.raw.{ApplyLambdaRawInliner, CallArrowRawInliner, CollectionRawInliner}
import aqua.raw.ops.*
import aqua.raw.value.*
import aqua.types.{ArrayType, OptionType, StreamType}
import cats.syntax.traverse.*
import cats.syntax.monoid.*
import cats.syntax.functor.*
import cats.syntax.flatMap.*
import cats.syntax.apply.*
import cats.instances.list.*
import cats.data.{Chain, State, StateT}
import scribe.Logging

object RawValueInliner extends Logging {

  import Inline.*

  private[inline] def unfold[S: Mangler: Exports: Arrows](
    raw: ValueRaw,
    lambdaAllowed: Boolean = true,
    canonicalizeStream: Boolean = true
  ): State[S, (ValueModel, Inline)] =
    raw match {
      case VarRaw(name, t) =>
        Exports[S].exports.map(VarModel(name, t, Chain.empty).resolveWith).map(_ -> Inline.empty)

      case LiteralRaw(value, t) =>
        State.pure(LiteralModel(value, t) -> Inline.empty)

      case alr: ApplyLambdaRaw =>
        ApplyLambdaRawInliner(alr, lambdaAllowed, canonicalizeStream)

      case cr: CollectionRaw =>
        CollectionRawInliner(cr, lambdaAllowed)

      case cr: CallArrowRaw =>
        CallArrowRawInliner(cr, lambdaAllowed)

      case sr: ShadowRaw =>
        // First, collect shadowed values
        // TODO: might be already defined in scope!
        sr.shadowValues.toList
          // Unfold/substitute all shadowed value
          .traverse { case (name, v) =>
            unfold(v, lambdaAllowed, canonicalizeStream).map { case (svm, si) =>
              (name, svm, si)
            }
          }.flatMap { fas =>
            val res = fas.map { case (n, v, _) =>
              n -> v
            }.toMap
            // Mark shadowed values as exports, isolate them into a scope
            Exports[S].exports
              .flatMap(curr =>
                Exports[S]
                  .scope(
                    Exports[S].resolved(res ++ curr.view.mapValues(_.resolveWith(res))) >>
                      // Resolve the value in the prepared Exports scope
                      unfold(sr.value, lambdaAllowed, canonicalizeStream)
                  )
              )
              .map { case (vm, inl) =>
                // Collect inlines to prepend before the value
                (vm, fas.map(_._3).foldLeft(inl)(_ |+| _))
              }
          }
    }

  private[inline] def inlineToTree[S: Mangler: Exports: Arrows](
    inline: Inline
  ): State[S, List[OpModel.Tree]] =
    inline.flattenValues.toList.traverse { case (name, v) =>
      valueToModel(v).map {
        case (vv, Some(op)) =>
          SeqModel.wrap(op, FlattenModel(vv, name).leaf)

        case (vv, _) =>
          FlattenModel(vv, name).leaf
      }
    }.map(inline.predo.toList ::: _)

  def valueToModel[S: Mangler: Exports: Arrows](
    value: ValueRaw,
    canonicalizeStream: Boolean = true
  ): State[S, (ValueModel, Option[OpModel.Tree])] =
    for {
      vmp <- unfold(value, canonicalizeStream = canonicalizeStream)
      (vm, map) = vmp

      _ = logger.trace("RAW " + value)
      _ = logger.trace("MOD " + vm)
      dc <- Exports[S].exports
      _ = logger.trace("DEC " + dc)

      ops <- inlineToTree(map)
      _ = logger.trace("desugarized ops: " + ops)
      _ = logger.trace("map was: " + map)
    } yield vm -> parDesugarPrefix(ops)

  def valueListToModel[S: Mangler: Exports: Arrows](
    values: List[ValueRaw],
    canonicalizeStream: Boolean = true
  ): State[S, List[(ValueModel, Option[OpModel.Tree])]] =
    values.traverse(valueToModel(_, canonicalizeStream))

  def callToModel[S: Mangler: Exports: Arrows](
    call: Call,
    canonicalizeStream: Boolean = true
  ): State[S, (CallModel, Option[OpModel.Tree])] =
    valueListToModel(call.args, canonicalizeStream).map { list =>
      (
        CallModel(
          list.map(_._1),
          call.exportTo.map(CallModel.callExport)
        ),
        parDesugarPrefix(list.flatMap(_._2))
      )
    }
}
