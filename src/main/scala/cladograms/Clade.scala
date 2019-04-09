package cladograms

/**
  * Created by Daniel on 4/7/2019.
  */
trait Clade {
  val name: String
  def ancestors: List[Clade]
}

case class SimpleClade(val name: String, val ancestors: List[Clade]) extends Clade

