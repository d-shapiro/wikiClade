package cladograms

/**
  * Created by Daniel on 5/11/2019.
  */
abstract class WikiClade extends Clade {
  val baseUrl = "https://en.wikipedia.org"
}

object WikiClade {
  def newClade(name: String): WikiClade = {

  }

}
