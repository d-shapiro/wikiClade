package cladograms

import org.jsoup.nodes.{Element, Document}
import org.jsoup.select.Elements

/**
  * Created by Daniel on 5/11/2019.
  */
abstract class WikiClade extends Clade {

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
    //TODO is this about me?

  }

  private def getDoc(path: Option[String]): Option[Document] = path match {
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



}
