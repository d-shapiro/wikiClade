package cladograms

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.io.File

import guru.nidi.graphviz.engine._
import guru.nidi.graphviz.engine.GraphvizServer

import scala.util.{Failure, Success, Try}

/**
  * Created by Daniel on 4/8/2019.
  */
object Main extends App {

  def getLeafClade(name: String): Clade = {
    EnWikipediaClade(name, Some("/wiki/" + name.replaceAll(" ", "_")))
  }

  def dotToGraphVizUrl(dot: String): String = {
    "https://dreampuf.github.io/GraphvizOnline/#" +
      URLEncoder.encode(dot, StandardCharsets.UTF_8.toString).replaceAll("\\+", " ")
  }

  val params = Set("-v", "-o")
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

  val dots: Set[(String, String)] = verbosity match {
    case None => cladSet map (cladogram => cladogram.clade.name -> cladogram.toDOT(cladogram.clade.name))
    case Some(s) => Try(s.toInt) match {
      case Success(v) => cladSet map (cladogram => cladogram.clade.name -> cladogram.toDOT(cladogram.clade.name, verbosity = v))
      case Failure(e) => throw new IllegalArgumentException("Verbosity must be an integer (0-100)")
    }
  }

  val outFolder = configs.get("-o") match {
    case None => "out"
    case Some(s) => s
  }

  dots.foreach {
    case (name, dot) =>
      println(dot + "\n")
      Graphviz.fromString(dot).render(Format.PNG).toFile(new File(s"$outFolder/$name.png"))
  }

  
}
