package cladograms

/**
  * Created by Daniel Shapiro on 4/7/2019.
  */
trait Clade {
  val name: String
  def ancestors: List[Clade]
  def shouldDisplay(verbosity: Int): Boolean = true
  def DOTDefinition: Option[String] = None
}
