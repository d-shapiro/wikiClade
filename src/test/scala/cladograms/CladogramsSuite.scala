package cladograms

/**
  * Created by Daniel on 4/7/2019.
  */
class CladogramsSuite {

  def test: Unit = {
    val a = SimpleClade("A", List())
    val b = SimpleClade("B", List(a))
    val c = SimpleClade("C", List(b, a))
    val d1 = SimpleClade("D1", List())
    val e1 = SimpleClade("E1", List(d1, c, a))
    val f1 = SimpleClade("F1", List(e1))
    val d2 = SimpleClade("D2", List(c, a))
    val e2 = SimpleClade("E2", List(d2, a))

    val cladSet = Cladogram.construct(List(e2, f1))
    require(cladSet.size == 1)
    println(cladSet.head.toDOT("Hello"))
  }

}
