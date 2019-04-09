//import net.ruippeixotog.scalascraper.dsl.DSL._
//import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
//import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
//import net.ruippeixotog.scalascraper.model._
//import net.ruippeixotog.scalascraper.browser.JsoupBrowser
//
//val browser = JsoupBrowser()
//val doc = browser.get("https://en.wikipedia.org/wiki/Euphorbia_milii")
//val e = doc >> extractor(".biota", element, asIs[Element]) >> extractor("tr", elementList, asIs[Element])
//def iter(l: List[Element]): Unit = {
//  l match {
//    case Nil => ()
//    case head :: tail => {
//      println(head >> allText("a"))
//      iter(tail)
//    }
//  }
//}
//iter(e)
import org.jsoup._
import org.jsoup.nodes._
import org.jsoup.select.Elements
val doc = Jsoup.connect("https://en.wikipedia.org/wiki/Euphorbia_milii").followRedirects(false).execute().parse()
val elems = doc.getElementsByClass("biota").get(0).select("tr")
def extractTaxonomy(elems: Elements): List[(String, String)] = {
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
  def iter(i: Int, started: Boolean, taxList: List[(String, String)]): List[(String, String)] = {
    if (i > elems.size()) {
      taxList
    } else {
      val row = elems.get(i)
      val ths = row.select("th")
      if (!started) {
        if (!ths.isEmpty && ths.get(0).text().equals("Scientific classification")) {
          iter(i + 1, true, taxList)
        } else {
          iter(i + 1, started, taxList)
        }
      } else {
        if (!ths.isEmpty) {
          taxList
        } else {
          iter(i + 1, started, parseRow(row) :: taxList)
        }
      }
    }
  }
  iter(0, false, List())
}
val tax = extractTaxonomy(elems)


Jsoup.connect("https://en.wikipedia.org/wiki/Rosids").execute().body().hashCode
