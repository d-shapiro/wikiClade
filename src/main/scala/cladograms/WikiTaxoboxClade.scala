package cladograms

import cladograms.WikiClade.TaxonDetails
import org.jsoup.select.Elements

import scala.util.Try

class WikiTaxoboxClade(val name: String, taxonomyPath: Option[String], details: Option[TaxonDetails] = None,
                       val priorityOverride: Double = 100) extends WikiClade {

  override def shouldDisplay(verbosity: Int): Boolean = priority <= verbosity

  override def getMeta: WikiCladeMetadata = details match {
    case None =>
      val docOpt = WikiClade.getDoc(taxonomyPath)
      val (lastItem: TaxonDetails, others: List[TaxonDetails]) = extractTaxonomy(WikiClade.getInfoTable(docOpt)) match {
        case Nil => throw new RuntimeException("Failed to parse taxonomy")
        case head :: tail => (head, tail)
      }
      val (mydetails: TaxonDetails, highestAnc: Option[TaxonDetails], taxonomy: List[TaxonDetails]) =
        others.reverse match {
        case Nil => (lastItem, None, Nil)
        case head :: tail => (head, Some(lastItem), tail)
      }

      val ancestors = (for {
        ancDetails <- taxonomy
      } yield {
        new WikiTaxoboxClade(ancDetails.name, None, Some(ancDetails))
      }) ++ (highestAnc match {
        case None => List()
        case Some(highestDetails) =>
          List(new WikiTaxoboxClade(highestDetails.name,
            Some(highestDetails.taxonomyPath.replaceAll("/skip", ""))))
      })

      WikiCladeMetadata(mydetails.name, ancestors, Some(mydetails.path),
        WikiClade.sanitizeCladeType(mydetails.cladeType), if (mydetails.isPrincipal) 30 else 80)
    case Some(mydetails) => WikiCladeMetadata(mydetails.name, List(), Some(mydetails.path),
      WikiClade.sanitizeCladeType(mydetails.cladeType), if (mydetails.isPrincipal) 30 else 80)
  }

  private def extractTaxonomy(elems: Elements): List[TaxonDetails] = {
    def iter(i: Int, taxList: List[TaxonDetails]): List[TaxonDetails] = {
      if (i < 0) {
        taxList
      } else {
        val row = elems.get(i)
        val tds = row.select("td")
        if (tds.size() < 3) {
          taxList
        } else {
          val principal = !tds.get(0).select("b").isEmpty
          val cladeType = tds.get(0).text()
          val refs = tds.get(1).select("a")
          val ref =
            if (refs.isEmpty) ""
            else refs.first().attr("href")
          val text =
            if (refs.isEmpty) tds.get(1).text()
            else refs.first().text()
          val tref = Try(tds.get(tds.size() - 1).select("a").first().attr("href")).getOrElse("")
          val details =
            TaxonDetails(text, cladeType, principal, if (ref.startsWith("/")) ref else "",
              if (tref.startsWith("/wiki/Template:Taxonomy")) tref else "")
          iter(i - 1, details :: taxList)
        }
      }
    }
    iter(elems.size() - 1, List())
  }
}
