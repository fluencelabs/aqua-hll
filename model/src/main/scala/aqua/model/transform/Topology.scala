package aqua.model.transform

import aqua.model.ValueModel
import aqua.model.body.{CallServiceTag, FuncOp, OnTag, OpTag, SeqTag}
import cats.Eval
import cats.data.Chain
import cats.free.Cofree

object Topology {
  type Tree = Cofree[Chain, OpTag]

  def through(peers: List[ValueModel]): Chain[Tree] =
    Chain.fromSeq(
      peers.map(FuncOp.noop).map(_.tree)
    )

  // TODO: after topology is resolved, OnTag should be eliminated
  def resolve(op: Tree): Tree =
    transformWithPath(op) {
      case (path, c: CallServiceTag, children) if c.peerId.isEmpty =>
        Cofree[Chain, OpTag](
          c.copy(peerId = path.collectFirst { case OnTag(peerId, _) =>
            peerId
          }),
          children
        )
      case (path, tag @ OnTag(pid, via), children) =>
        // Drop seq/par/xor from path
        val pathOn = path.collect { case ot: OnTag =>
          ot
        }

        pathOn match {
          // If we are on the right node, do nothing
          case Nil =>
            Cofree[Chain, OpTag](tag, children)
          case h :: _ if h.peerId == pid =>
            Cofree[Chain, OpTag](tag, children)
          case h :: _ =>
            Cofree[Chain, OpTag](
              tag,
              // TODO: merge children, if possible
              children.map(through(h.via.reverse ++ via) ++ _)
            )
        }

      case (path, SeqTag, children) =>
        Cofree[Chain, OpTag](
          SeqTag,
          children.map(c =>
            // TODO if we have OnTag, and then something else, need to get back
            c
          )
        )

      case (_, t, children) =>
        Cofree[Chain, OpTag](t, children)
    }

  def transformWithPath(cf: Tree, path: List[OpTag] = Nil)(
    f: (List[OpTag], OpTag, Eval[Chain[Tree]]) => Tree
  ): Tree = {
    val newCf = f(path, cf.head, cf.tail)
    Cofree[Chain, OpTag](
      newCf.head,
      newCf.tail.map(_.map(transformWithPath(_, newCf.head :: path)(f)))
    )
  }
}
