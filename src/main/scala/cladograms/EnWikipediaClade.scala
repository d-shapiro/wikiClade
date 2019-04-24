package cladograms

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements

import scala.util.{Failure, Success, Try}
/**
  * Created by Daniel on 4/8/2019.
  */
case class EnWikipediaClade(val name: String, val path: Option[String], val priorityOverride: Double = 100) extends Clade {
  val baseUrl = "https://en.wikipedia.org"
  val ignorableCladeTypes = Set("Clade", "(unranked)")
  val importantCladeTypes = Set("Kingdom", "Phylum", "Class", "Order", "Family", "Genus", "Species")

  lazy val meta: WikiCladeMetadata = getMeta

  def ancestors: List[Clade] = meta.ancestors
  def priority: Double = Math.min(
    Math.min(priorityOverride, meta.docPriority),
    if (importantCladeTypes contains meta.cladeType) 20 else 100)

  override def shouldDisplay(verbosity: Int): Boolean = priority <= verbosity

  override def DOTDefinition: Option[String] = {
    val cladeTypeStr = if (meta.cladeType.isEmpty) "" else s"""<FONT POINT-SIZE=\"10\">${meta.cladeType}</FONT><br/>"""
    val hrefStr = path match {
      case None => ""
      case Some(p) => s"""href="$baseUrl$p","""
    }
    Some(s""""$name" [$hrefStr label=<$cladeTypeStr<B>$name</B>>]""")
  }

  private def getMeta: WikiCladeMetadata = {
    val docOpt = getDoc
    val (taxonomy, cladeType) = extractTaxonomy(getInfoTable(docOpt)) match {
      case Nil => (Nil, "")
      case head :: tail => (tail, head.cladeType)
    }
    val ancestors = for {
      details <- taxonomy
    } yield {
      if (details.path.isEmpty) new EnWikipediaClade(details.name, None)
      else new EnWikipediaClade(details.name, Some(details.path))
    }
    val docPriority = priorityBasedOnDoc(docOpt)
    WikiCladeMetadata(ancestors, sanitizeCladeType(cladeType), docPriority)
  }

  private def getDoc: Option[Document] = path match {
    case Some(pathStr) => Some(Jsoup.connect(baseUrl + pathStr).execute().parse())
    case None => None
  }

  private def getInfoTable(docOpt: Option[Document]): Elements = docOpt match {
    case Some(doc) => {
      val biotas = doc.getElementsByClass("biota")
      if (biotas.isEmpty) {
        new Elements()
      } else {
        biotas.get(0).select("tr")
      }
    }
    case None => new Elements()
  }

  private def extractTaxonomy(elems: Elements): List[TaxonDetails] = {
    def parseRow(row: Element): TaxonDetails = {
      val tds = row.select("td")
      if (tds.isEmpty) TaxonDetails("", "", "")
      else {
        val td = tds.get(tds.size() - 1)
        val refs = td.select("a")
        val ref =
          if (refs.isEmpty) ""
          else refs.first().attr("href")
        val text = Try(td.child(0)).getOrElse(td).text()
        val cladeType = if (tds.size() > 1) tds.get(tds.size() - 2).text() else ""
        TaxonDetails(text, cladeType, ref)
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
            iter(i + 1, true, knownPages, taxList)
          } else {
            iter(i + 1, started, knownPages, taxList)
          }
        } else {
          if (!ths.isEmpty) {
            taxList
          } else {
            val details = parseRow(row)
            if (details.path.nonEmpty) {
              val pagetry = Try(Jsoup.connect(baseUrl + details.path).get().select("title").text())
              pagetry match {
                case Success(page) => if (knownPages contains page) {
                  iter(i + 1, started, knownPages, TaxonDetails(details.name, details.cladeType, "") :: taxList)
                } else {
                  iter(i + 1, started, knownPages + page, details :: taxList)
                }
                case Failure(_) =>
                  iter(i + 1, started, knownPages, TaxonDetails(details.name, details.cladeType, "") :: taxList)
              }
            } else {
              iter(i + 1, started, knownPages, details :: taxList)
            }
          }
        }
      }
    }
    iter(0, false, Set(), List())
  }

  private def sanitizeCladeType(cladeType: String): String = {
    val cleaned = cladeType.replaceAll(":", "").trim
    if (ignorableCladeTypes contains cleaned) "" else cleaned
  }

  def priorityBasedOnDoc(docOpt: Option[Document]): Double = docOpt match {
    case Some(doc) => Math.min (99, Math.max (1, 100 - (15 * (Math.log (doc.text ().length) - 7) ) ) )
    case None => 99
  }

  case class WikiCladeMetadata(ancestors: List[Clade], cladeType: String, docPriority: Double)

  case class TaxonDetails(name: String, cladeType: String, path: String)

}
