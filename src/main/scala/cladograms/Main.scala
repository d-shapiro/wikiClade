package cladograms

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.io.File

import com.sun.org.apache.xml.internal.serialize.OutputFormat
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

  val params = Set("-v", "-o", "-f")
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

  val dot: String = verbosity match {
    case None => Cladogram.toDOT(cladSet, "Cladogram")
    case Some(s) => Try(s.toInt) match {
      case Success(v) => Cladogram.toDOT(cladSet, "Cladogram", verbosity = v)
      case Failure(_) => throw new IllegalArgumentException("Verbosity must be an integer (0-100)")
    }
  }

  object OutFormat {
    sealed abstract class OutFormat(val gvFormat: Format, val fileExtension: String)
    case object PNG extends OutFormat(Format.PNG, ".png")
    case object SVG extends OutFormat(Format.SVG, ".svg")
    case object DOT extends OutFormat(Format.XDOT, "")
    def fromString(s: String): Option[OutFormat] = {
      Vector(PNG, SVG, DOT).find(_.toString == s)
    }
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

  Graphviz.fromString(dot).render(format.gvFormat).toFile(new File(s"$outFile"))
}
