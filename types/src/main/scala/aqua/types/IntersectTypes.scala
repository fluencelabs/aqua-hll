package aqua.types

import cats.Monoid
import cats.data.NonEmptyMap

object IntersectTypes extends Monoid[Type]:

  override def empty: Type = TopType

  def combineProducts(ap: ProductType, bp: ProductType): ProductType =
    ProductType(ap.toList.zip(bp.toList).map(combine))

  override def combine(a: Type, b: Type): Type =
    CompareTypes.apply(a, b) match {
      case 1.0 => b
      case -1.0 => a
      case 0.0 => b
      case Double.NaN =>
        // Uncomparable types
        // But can we intersect them?
        // And find such a type that a, b both subtype?
        (a, b) match {
          case (ap: ProductType, bp: ProductType) =>
            combineProducts(ap, bp)

          case (as: StructType, bs: StructType) =>
            NonEmptyMap
              .fromMap(as.fields.toSortedMap.flatMap { case (ak, at) =>
                bs.fields.lookup(ak).map(bt => ak -> combine(at, bt))
              })
              .fold(empty)(fields => StructType(s"${as.name} ∩ ${bs.name}", fields))

          case (aa: ArrowType, bb: ArrowType) =>
            // TODO test that both aa, bb are subtypes of this arrow
            ArrowType(
              UniteTypes.combineProducts(aa.domain, bb.domain),
              combineProducts(aa.codomain, bb.codomain)
            )

          case (ac: OptionType, bc: BoxType) =>
            OptionType(ac.element `∩` bc.element)

          case (ac: BoxType, bc: OptionType) =>
            OptionType(ac.element `∩` bc.element)

          case (ac: ArrayType, bc: BoxType) =>
            ArrayType(ac.element `∩` bc.element)
          case (ac: BoxType, bc: ArrayType) =>
            ArrayType(ac.element `∩` bc.element)
          case (ac: StreamType, bc: StreamType) =>
            StreamType(ac.element `∩` bc.element)

          case _ =>
            empty
        }

    }
