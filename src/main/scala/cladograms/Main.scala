package cladograms

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import org.jsoup.Jsoup

import scala.util.{Failure, Success, Try}

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

  val params = Set("-v")
  val flags = Set()

  def parseArgs: (List[String], Map[String, String]) = {
    def iter(args: List[String], param: Option[String], inputs: List[String], configs: Map[String, String]):
    (List[String], Map[String, String]) = args match {
      case Nil => (inputs, configs)
      case arg :: theRest => param match {
        case None =>
          if (params contains arg) iter(theRest, Some(arg), inputs, configs)
          else iter(theRest, None, arg :: inputs, configs)
        case Some(s) => iter(theRest, None, inputs, configs + (s -> arg))
      }
    }
    iter(args.toList, None, List(), Map())
  }

  val (inputs, configs) = parseArgs
  val leaves = inputs.reverse map getLeafClade
  val cladSet = Cladogram.construct(leaves)

  val verbosity = configs.get("-v")

  val dots = verbosity match {
    case None => cladSet map (cladogram => cladogram.toDOT(cladogram.clade.name))
    case Some(s) => Try(s.toInt) match {
      case Success(v) => cladSet map (cladogram => cladogram.toDOT(cladogram.clade.name, verbosity = v))
      case Failure(e) => throw new IllegalArgumentException("Verbosity must be an integer (0-100)")
    }
  }
  dots.foreach (dot => println(dot + "\n" + dotToGraphVizUrl(dot) + "\n"))
}
