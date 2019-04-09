package cladograms

import org.jsoup.Jsoup

/**
  * Created by Daniel on 4/8/2019.
  */
object Main extends App {

  def getLeafClade(name: String): Clade = {
    new EnWikipediaClade(name, Some("/wiki/" + name.replaceAll(" ", "_")))
  }

  val leaves = (args map getLeafClade).toList
  val cladSet = Cladogram.construct(leaves)
  val dots = cladSet map (cladogram => cladogram.toDOT(cladogram.clade.name))

  dots.foreach (dot => println(dot + "\n"))
}
