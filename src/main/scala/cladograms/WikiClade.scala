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

  case class WikiCladeMetadata(ancestors: List[Clade], cladeType: String, docPriority: Double)
}

object WikiClade {
  val baseUrl = "https://en.wikipedia.org"
  def newInputClade(name: String): WikiClade = {
    val path = "/wiki/" + name.replaceAll(" ", "_")
    val url = baseUrl + path
    val docOpt = WikiProxy.fetchPage(url)
    //TODO Check for paraphyly
    //TODO Check for polyphyly
    val taxDocOpt = getDoc(getTaxoboxLink(docOpt))
    val title = getTitle(Some(path))
    val ttitle = getTitle(taxoboxGetPageLink(taxDocOpt))
    if (title == ttitle) {
      //TODO taxobox clade
    } else {
      new WikiPageClade(name, Some(path), 0)
    }
  }

  def newClade(name: String): WikiClade = {
    val taxPath = "/wiki/Template:Taxonomy/" + name.replaceAll(" ", "_")
    getDoc(Some(taxPath)) match {
      case Some(_) => //TODO taxobox clade
      case None => new WikiPageClade(name, None)
    }
  }

  def newClade(name: String, path: String): WikiClade = {
    val taxPath = path.replaceAll("wiki/", "wiki/Template:Taxonomy/")
    getDoc(Some(taxPath)) match {
      case Some(_) => //TODO taxobox clade
      case None => new WikiPageClade(name, None)
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



}
