package cladograms

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.io.File

import cladograms.Main.OutFormat.OutFormat
import guru.nidi.graphviz.engine._
import scala.util.{Failure, Success, Try}

/**
  * Created by Daniel Shapiro on 4/8/2019.
  */
object Main extends App {

  object OutFormat {
    sealed abstract class OutFormat(val gvFormat: Format, val fileExtension: String)
    case object PNG extends OutFormat(Format.PNG, ".png")
    case object SVG extends OutFormat(Format.SVG, ".svg")
    case object DOT extends OutFormat(Format.XDOT, "")
    def fromString(s: String): Option[OutFormat] = {
      Vector(PNG, SVG, DOT).find(_.toString == s)
    }
  }

  def getLeafClade(name: String): Clade = {
    WikiClade.newInputClade(name)
  }

  def dotToGraphVizUrl(dot: String): String = {
    "https://dreampuf.github.io/GraphvizOnline/#" +
      URLEncoder.encode(dot, StandardCharsets.UTF_8.toString).replaceAll("\\+", " ")
  }

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

  def renderer(inputs: List[String], verbosity: Option[Int], format: OutFormat): Renderer = {
    val leaves = inputs.reverse map getLeafClade
    val cladSet = Cladogram.construct(leaves)

    val dot: String = verbosity match {
      case None => Cladogram.toDOT(cladSet, "Cladogram")
      case Some(v) => Cladogram.toDOT(cladSet, "Cladogram", verbosity = v)
    }

    Graphviz.fromString(dot).render(format.gvFormat)
  }

  def svg(inputs: List[String], verbosity: Option[Int]): String = {
    renderer(inputs, verbosity, OutFormat.SVG).toString
  }

  val params = Set("-v", "-o", "-f")
  val flags = Set()

  val (inputs, configs) = parseArgs
  val verbosity = configs.get("-v") match {
    case None => None
    case Some(s) => Try(s.toInt).toOption
  }

  val format = configs.get("-f") match {
    case None => OutFormat.DOT
    case Some(s) => OutFormat.fromString(s.toUpperCase) match {
      case Some(f) => f
      case None => OutFormat.DOT
    }
  }

  val outFile = configs.get("-o") match {
    case None => "out" + format.fileExtension
    case Some(s) => if (s.endsWith(format.fileExtension)) s else s + format.fileExtension
  }

  renderer(inputs, verbosity, format).toFile(new File(s"$outFile"))
}
