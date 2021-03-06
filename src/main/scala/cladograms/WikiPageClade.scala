package cladograms

import cladograms.WikiClade.TaxonDetails
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements

import scala.util.Try

/**
  * Created by Daniel on 5/11/2019.
  */
class WikiPageClade(val name: String, path: Option[String], val priorityOverride: Double = 100) extends WikiClade {

  override def shouldDisplay(verbosity: Int): Boolean = priority <= verbosity

  override def getMeta: WikiCladeMetadata = {
    val docOpt = WikiClade.getDoc(path)
    val (taxonomy, mydetails) = extractTaxonomy(WikiClade.getInfoTable(docOpt)) match {
      case Nil => (Nil, taxonDetails(name, "", ""))
      case head :: tail => (tail, head)
    }
    val ancestors = for {
      details <- taxonomy
    } yield {
      if (details.path.isEmpty) WikiClade.newClade(details.name, None)
      else WikiClade.newClade(details.name, Some(details.path))
    }
    val docPriority = priorityBasedOnDoc(docOpt)
    WikiCladeMetadata(mydetails.name ,ancestors, path, WikiClade.sanitizeCladeType(mydetails.cladeType), docPriority,
      mydetails.path.nonEmpty && !mydetails.cladeType.contains("pecies"))
  }

  private def extractTaxonomy(elems: Elements): List[TaxonDetails] = {
    def parseRow(row: Element): TaxonDetails = {
      val tds = row.select("td")
      if (tds.isEmpty) taxonDetails("", "", "")
      else {
        val td = tds.get(tds.size() - 1)
        val brs = td.select("br")
        val tdline =
          if (brs.isEmpty) td
          else brs.get(0).previousElementSibling()
        val refs = tdline.select("a")
        val ref =
          if (refs.isEmpty) ""
          else refs.first().attr("href")
        val text = Try(tdline.select(":not(span)").get(0)).getOrElse(tdline).text()
          .replaceAll("\\[[0-9]*\\]", "").replaceAll("†", "")
        val cladeType = if (tds.size() > 1) tds.get(tds.size() - 2).text() else ""
        taxonDetails(text, cladeType, if (ref.startsWith("/")) ref else "")
      }
    }
    def iter(i: Int, started: Boolean, knownPages: Set[String], taxList: List[TaxonDetails]):
    List[TaxonDetails] = {
      if (i >= elems.size()) {
        taxList
      } else {
        val row = elems.get(i)
        val ths = row.select("th")
        if (!started) {
          if (!ths.isEmpty && ths.get(0).text().equals("Scientific classification")) {
            val updKnownPages = path match {
              case Some(pathStr) =>
                WikiProxy.getTitle(WikiClade.baseUrl + pathStr) match {
                  case Some(title) => knownPages + title
                  case None => knownPages
                }
              case None => knownPages
            }
            iter(i + 1, started=true, updKnownPages, taxList)
          } else {
            iter(i + 1, started, knownPages, taxList)
          }
        } else {
          if (!ths.isEmpty) {
            taxList
          } else {
            val details = parseRow(row)
            if (details.path.nonEmpty) {
              WikiProxy.getTitle(WikiClade.baseUrl + details.path) match {
                case Some(page) => if (knownPages contains page) {
                  iter(i + 1, started, knownPages, taxonDetails(details.name, details.cladeType, "") :: taxList)
                } else {
                  iter(i + 1, started, knownPages + page, details :: taxList)
                }
                case None =>
                  iter(i + 1, started, knownPages, taxonDetails(details.name, details.cladeType, "") :: taxList)
              }
            } else {
              iter(i + 1, started, knownPages, details :: taxList)
            }
          }
        }
      }
    }
    iter(0, started=false, Set(), List())
  }

  def priorityBasedOnDoc(docOpt: Option[Document]): Double = docOpt match {
    case Some(doc) => Math.min (99, Math.max (1, 100 - (15 * (Math.log (doc.text ().length) - 7) ) ) )
    case None => 99
  }

  private def taxonDetails(name: String, cladeType: String, path: String): TaxonDetails = {
    TaxonDetails(name, cladeType, isPrincipal=false, path, "")
  }

}
