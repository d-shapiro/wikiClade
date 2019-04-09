package cladograms

/**
  * Created by Daniel on 4/7/2019.
  */
class Cladogram (val clade: Clade, var children: Set[Cladogram]) {
  def incl(child: Cladogram): Unit =
    children = children + child

  def merge(other: Cladogram): Unit = {
    require(clade == other.clade)
    children = children ++ other.children
  }

  def toDOT(name: String): String = {
    "digraph " + name + " {\n" +
    partialToDOT.mkString +
    "}"
  }

  def partialToDOT: List[String] = {
    val toplevel = (for {
      child <- children
    } yield "\"" +clade.name + "\" -> \"" + child.clade.name + "\";\n").toList
    toplevel ++ children.flatMap(_.partialToDOT)
  }
}

object Cladogram {
  def expand(clade: Clade): Cladogram = {
    def runningExpand(lineage: Cladogram, unexplored: List[Clade]): Cladogram = {
      lineage.clade.ancestors match {
        case List() => unexplored match {
          case List() => lineage
          case head :: tail => runningExpand(new Cladogram(head, Set(lineage)), tail)
        }
        case parent :: others => runningExpand(new Cladogram(parent, Set(lineage)), others)
      }
    }
    runningExpand(new Cladogram(clade, Set()), List())
  }


  def construct(leaves: List[Clade]): Set[Cladogram] = {

    def expand(lineage: Cladogram, known: Map[Clade, Cladogram], roots: Set[Cladogram], ancestryHints: List[Clade]):
    (Map[Clade, Cladogram], Set[Cladogram]) = {
      if (known contains lineage.clade) {
        known(lineage.clade) merge lineage
        (known, roots)
      } else {
        val newKnown = known + (lineage.clade -> lineage)
        lineage.clade.ancestors match {
          case List() => ancestryHints match {
            case List() => (newKnown, roots + lineage)
            case parent :: grandparents => expand(new Cladogram(parent, Set(lineage)), newKnown, roots, grandparents)
          }
          case parent :: grandparents => expand(new Cladogram(parent, Set(lineage)), newKnown, roots, grandparents)
        }
      }
    }

    def iter(remaining: List[Clade], known: Map[Clade, Cladogram], roots: Set[Cladogram]): Set[Cladogram] = {
      remaining match {
        case List() => roots
        case head :: tail => {
          val (updKnown, updRoots) = expand(new Cladogram(head, Set()), known, roots, List())
          iter(tail, updKnown, updRoots)
        }
      }
    }
    iter(leaves, Map(), Set())
  }


}
