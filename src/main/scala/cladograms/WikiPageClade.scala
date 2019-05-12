package cladograms

/**
  * Created by Daniel on 5/11/2019.
  */
case class WikiPageClade(name: String) extends WikiClade {

  lazy val ancestors = getAncestors

  def getAncestors: List[Clade] = {

  }

}
