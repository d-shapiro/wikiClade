package cladograms

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import org.jsoup.Jsoup

/**
  * Created by Daniel on 4/8/2019.
  */
object Main extends App {

  def getLeafClade(name: String): Clade = {
    new EnWikipediaClade(name, Some("/wiki/" + name.replaceAll(" ", "_")))
  }

  def dotToGraphVizUrl(dot: String): String = {
    "https://dreampuf.github.io/GraphvizOnline/#" +
      URLEncoder.encode(dot, StandardCharsets.UTF_8.toString).replaceAll("\\+", " ")
  }

  val leaves = (args map getLeafClade).toList
  val cladSet = Cladogram.construct(leaves)
  val dots = cladSet map (cladogram => cladogram.toDOT(cladogram.clade.name))

  dots.foreach (dot => println(dot + "\n" + dotToGraphVizUrl(dot) + "\n"))
}
