package cladograms

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements

import scala.util.{Failure, Success, Try}

/**
  * Created by Daniel on 4/8/2019.
  */
case class EnWikipediaClade(val name: String, val path: Option[String], val priority: Double = 0) extends Clade {
  val baseUrl = "https://en.wikipedia.org"

  lazy val ancestors = constructAncestry

  override def shouldDisplay(verbosity: Int) = priority <= verbosity


  private def constructAncestry: List[Clade] = {
    val taxonomy = extractTaxonomy(getInfoTable) match {
      case Nil => Nil
      case head :: tail => tail
    }
    for {
      (taxName, taxPath, priority) <- taxonomy
    } yield {
      if (taxPath.isEmpty) new EnWikipediaClade(taxName, None, priority)
      else new EnWikipediaClade(taxName, Some(taxPath), priority)
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

  private def extractTaxonomy(elems: Elements): List[(String, String, Double)] = {
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
    def iter(i: Int, started: Boolean, knownPages: Set[String], taxList: List[(String, String, Double)]):
    List[(String, String, Double)] = {
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
              val doctry = Try(Jsoup.connect(baseUrl + path).get())
              val pagetry = doctry match {
                case Success(doc) => Try(doc.select("title").text())
                case Failure(e) => Failure(e)
              }
              pagetry match {
                case Success(page) => if (knownPages contains page) {
                  iter(i + 1, started, knownPages, (name, "", 99.0) :: taxList)
                } else {
                  val pri = priorityBasedOnDoc(doctry.get)
                  iter(i + 1, started, knownPages + page, (name, path, pri) :: taxList)
                }
                case Failure(_) => iter(i + 1, started, knownPages, (name, "", 99.0) :: taxList)
              }
            } else {
              iter(i + 1, started, knownPages, (name, path, 99.0) :: taxList)
            }
          }
        }
      }
    }
    iter(0, false, Set(), List())
  }

  def priorityBasedOnDoc(doc: Document): Double =
    Math.min(99, Math.max(1, 100 - (15 * (Math.log(doc.text().length) - 7))))


}
