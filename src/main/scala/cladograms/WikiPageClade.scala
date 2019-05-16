package cladograms

import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements

import scala.util.Try

/**
  * Created by Daniel on 5/11/2019.
  */
class WikiPageClade(val name: String, path: Option[String], priorityOverride: Double = 100) extends WikiClade {
  val ignorableCladeTypes: Set[String] = Set("Clade", "(unranked)")
  val importantCladeTypes: Set[String] = Set("Kingdom", "Phylum", "Class", "Order", "Family", "Genus", "Species")

  def priority: Double = Math.min(
    Math.min(priorityOverride, meta.docPriority),
    if (importantCladeTypes contains meta.cladeType) 20 else 100)

  override def shouldDisplay(verbosity: Int): Boolean = priority <= verbosity

  override def DOTDefinition: Option[String] = {
    val cladeTypeStr = if (meta.cladeType.isEmpty) "" else s"""<FONT POINT-SIZE=\"10\">${meta.cladeType}</FONT><br/>"""
    val hrefStr = path match {
      case None => ""
      case Some(p) => s"""href="$WikiClade.baseUrl$p","""
    }
    Some(s""""$name" [$hrefStr label=<$cladeTypeStr<B>$name</B>>]""")
  }

  override def getMeta: WikiCladeMetadata = {
    val docOpt = WikiClade.getDoc(path)
    val (taxonomy, cladeType) = extractTaxonomy(WikiClade.getInfoTable(docOpt)) match {
      case Nil => (Nil, "")
      case head :: tail => (tail, head.cladeType)
    }
    val ancestors = for {
      details <- taxonomy
    } yield {
      if (details.path.isEmpty) WikiClade.newClade(details.name)
      else WikiClade.newClade(details.name, details.path)
    }
    val docPriority = priorityBasedOnDoc(docOpt)
    WikiCladeMetadata(ancestors, sanitizeCladeType(cladeType), docPriority)
  }

  private def extractTaxonomy(elems: Elements): List[TaxonDetails] = {
    def parseRow(row: Element): TaxonDetails = {
      val tds = row.select("td")
      if (tds.isEmpty) TaxonDetails("", "", "")
      else {
        val td = tds.get(tds.size() - 1)
        val refs = Try(td.child(0)).getOrElse(td).select("a")
        val ref =
          if (refs.isEmpty) ""
          else refs.first().attr("href")
        val text = Try(td.child(0)).getOrElse(td).text()
        val cladeType = if (tds.size() > 1) tds.get(tds.size() - 2).text() else ""
        TaxonDetails(text, cladeType, if (ref.startsWith("/")) ref else "")
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
                  iter(i + 1, started, knownPages, TaxonDetails(details.name, details.cladeType, "") :: taxList)
                } else {
                  iter(i + 1, started, knownPages + page, details :: taxList)
                }
                case None =>
                  iter(i + 1, started, knownPages, TaxonDetails(details.name, details.cladeType, "") :: taxList)
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

  private def sanitizeCladeType(cladeType: String): String = {
    val cleaned = cladeType.replaceAll(":", "").trim
    if (ignorableCladeTypes contains cleaned) "" else cleaned
  }

  def priorityBasedOnDoc(docOpt: Option[Document]): Double = docOpt match {
    case Some(doc) => Math.min (99, Math.max (1, 100 - (15 * (Math.log (doc.text ().length) - 7) ) ) )
    case None => 99
  }

  def canEqual(obj: Any): Boolean = obj.isInstanceOf[WikiPageClade]
  override def equals(obj: Any): Boolean = obj match {
    case obj: WikiPageClade => obj.canEqual(this) && this.name == obj.name //TODO is this sufficient?
    case _ => false
  }

  override def hashCode(): Int = name.hashCode

  case class TaxonDetails(name: String, cladeType: String, path: String)

}
