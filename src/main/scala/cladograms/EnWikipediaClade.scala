package cladograms

import fastily.jwiki.core._
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import scala.util.{Failure, Success, Try}

/**
  * Created by Daniel on 4/8/2019.
  */
case class EnWikipediaClade(val name: String, val path: Option[String]) extends Clade{
  val baseUrl = "https://en.wikipedia.org"

  lazy val ancestors = constructAncestry

  private def constructAncestry: List[Clade] = {
    val taxonomy = extractTaxonomy(getInfoTable) match {
      case Nil => Nil
      case head :: tail => tail
    }
    for {
      (taxName, taxPath) <- taxonomy
    } yield {
      if (taxPath.isEmpty) new EnWikipediaClade(taxName, None)
      else new EnWikipediaClade(taxName, Some(taxPath))
    }
  }

  private def getInfoTable: Elements = path match {
    case Some(pathStr) => {
      val doc = Jsoup.connect(baseUrl + pathStr).execute().parse()
      val biotas = doc.getElementsByClass("biota")
      if (biotas.isEmpty) {
        new Elements()
      } else {
        biotas.get(0).select("tr")
      }
    }
    case None => new Elements()
  }

  private def extractTaxonomy(elems: Elements): List[(String, String)] = {
    def parseRow(row: Element): (String, String) = {
      val tds = row.select("td")
      if (tds.isEmpty) ("", "")
      else {
        val td = tds.get(tds.size() - 1)
        val refs = td.select("a")
        val ref =
          if (refs.isEmpty) ""
          else refs.first().attr("href")
        val text = td.text()
        (text, ref)
      }
    }
    def iter(i: Int, started: Boolean, knownPages: Set[String], taxList: List[(String, String)]): List[(String, String)] = {
      if (i >= elems.size()) {
        taxList
      } else {
        val row = elems.get(i)
        val ths = row.select("th")
        if (!started) {
          if (!ths.isEmpty && ths.get(0).text().equals("Scientific classification")) {
            iter(i + 1, true, knownPages, taxList)
          } else {
            iter(i + 1, started, knownPages, taxList)
          }
        } else {
          if (!ths.isEmpty) {
            taxList
          } else {
            val (name, path) = parseRow(row)
            if (path.nonEmpty) {
              val pagetry = Try(Jsoup.connect(baseUrl + path).get().select("title").text())
              pagetry match {
                case Success(page) => if (knownPages contains page) {
                  iter(i + 1, started, knownPages, (name, "") :: taxList)
                } else {
                  iter(i + 1, started, knownPages + page, (name, path) :: taxList)
                }
                case Failure(_) => iter(i + 1, started, knownPages, (name, "") :: taxList)
              }
            } else {
              iter(i + 1, started, knownPages, (name, path) :: taxList)
            }
          }
        }
      }
    }
    iter(0, false, Set(), List())
  }

}
