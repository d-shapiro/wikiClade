package cladograms

import org.jsoup.nodes.{Element, Document}
import org.jsoup.select.Elements

/**
  * Created by Daniel on 5/11/2019.
  */
abstract class WikiClade extends Clade {
  lazy val meta: WikiCladeMetadata = getMeta

  override def ancestors: List[Clade] = meta.ancestors

  def getMeta: WikiCladeMetadata

  def priorityOverride: Double

  case class WikiCladeMetadata(trueName: String, ancestors: List[Clade], path: Option[String], cladeType: String, docPriority: Double)

  override def DOTDefinition: Option[String] = {
    val cladeTypeStr = if (meta.cladeType.isEmpty) "" else s"""<FONT POINT-SIZE=\"10\">${meta.cladeType}</FONT><br/>"""
    val hrefStr = meta.path match {
      case None => ""
      case Some(p) => s"""href="${WikiClade.baseUrl}${p.replaceAll("&", "&amp;")}","""
    }
    val dispname =
      if (meta.trueName == name) name
      else s"${meta.trueName}<br/>($name)"
    Some(s""""$name" [$hrefStr label=<$cladeTypeStr<B>$dispname</B>>]""")
  }

  def priority: Double = Math.min(
    Math.min(priorityOverride, meta.docPriority),
    if (WikiClade.importantCladeTypes contains meta.cladeType) 20 else 100)
}

object WikiClade {
  val baseUrl = "https://en.wikipedia.org"
  val ignorableCladeTypes: Set[String] = Set("Clade", "(unranked)")
  val importantCladeTypes: Set[String] = Set("Kingdom", "Phylum", "Class", "Order", "Family", "Genus", "Species")

  def newInputClade(name: String): WikiClade = {
    val path = "/wiki/" + name.replaceAll(" ", "_")
    val url = baseUrl + path
    val docOpt = WikiProxy.fetchPage(url)
    //TODO Check for paraphyly
    //TODO Check for polyphyly
    val taxPathOpt = getTaxoboxLink(docOpt)
    val taxDocOpt = getDoc(taxPathOpt)
    val title = getTitle(Some(path))
    val ttitle = getTitle(taxoboxGetPageLink(taxDocOpt))
    title match {
      case None => new WikiPageClade(name, taxPathOpt, 0)
      case Some(_) =>
        if (title == ttitle) new WikiTaxoboxClade(name, taxPathOpt, priorityOverride = 0)
        else new WikiPageClade(name, Some(path), 0)
    }
  }

  def newClade(name: String, path: Option[String]): WikiClade = {
    val taxPath = "/wiki/Template:Taxonomy/" + name.replaceAll(" ", "_")
    getTitle(Some(taxPath)) match {
      case Some(_) => new WikiTaxoboxClade(name, Some(taxPath))
      case None => new WikiPageClade(name, path)
    }
  }

  def getTitle(path: Option[String]): Option[String] = path match {
    case Some(pathStr) => WikiProxy.getTitle(baseUrl + pathStr)
    case None => None
  }

  def getDoc(path: Option[String]): Option[Document] = path match {
    case Some(pathStr) => WikiProxy.fetchPage(baseUrl + pathStr)
    case None => None
  }

  def getInfoTable(docOpt: Option[Document]): Elements = docOpt match {
    case Some(doc) =>
      val biotas = doc.getElementsByClass("biota")
      if (biotas.isEmpty) {
        new Elements()
      } else {
        biotas.get(0).select("tr")
      }
    case None => new Elements()
  }

  private def getTaxoboxLink(docOpt: Option[Document]): Option[String] = {
    val elements = getInfoTable(docOpt)
    def iter(i: Int): Option[String] = {
      def iter2(as: Elements, j: Int): Option[String] = {
        if (j >= as.size()) None
        else {
          val a = as.get(j)
          if (a.attr("href").contains("Template:Taxonomy")) Some(a.attr("href"))
          else iter2(as, j+1)
        }
      }

      if (i >= elements.size()) None
      else {
        val row = elements.get(i)
        val ths = row.select("th")
        if (!ths.isEmpty && ths.get(0).text().equals("Scientific classification")) iter2(ths.get(0).select("a"), 0)
        else iter(i + 1)
      }
    }
    iter(0)
  }

  private def taxoboxGetWikiTable(taxDocOpt: Option[Document]): Elements = taxDocOpt match {
    case Some(doc) =>
      val wikitables = doc.getElementsByClass("wikitable")
      if (wikitables.isEmpty) new Elements()
      else wikitables.get(0).select("tr")
    case None => new Elements()
  }

  private def taxoboxGetPageLink(taxDocOpt: Option[Document]): Option[String] = {
    val elements = taxoboxGetWikiTable(taxDocOpt)
    def iter(i: Int): Option[String] = {
      if (i >= elements.size()) None
      else {
        val row = elements.get(i)
        val tds = row.select("td")
        if (tds.size < 2) iter(i + 1)
        else {
          val td0 = tds.get(0)
          val td1 = tds.get(tds.size() - 1)
          if (td0.text.startsWith("Link")) {
            val refs = td1.select("a")
            if (refs.isEmpty) None
            else Some(refs.first.attr("href"))
          } else iter(i + 1)
        }
      }
    }
    iter(0)
  }

  def sanitizeCladeType(cladeType: String): String = {
    val cleaned = cladeType.replaceAll(":", "").trim
    if (ignorableCladeTypes contains cleaned) "" else cleaned
  }

  case class TaxonDetails(name: String, cladeType: String, isPrincipal: Boolean,
                          path: String, taxonomyPath: String)
}
