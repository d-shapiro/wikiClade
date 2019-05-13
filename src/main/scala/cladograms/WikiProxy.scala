package cladograms

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.collection.mutable
import scala.util.Try

object WikiProxy {

  private val pageTitles: mutable.Map[String, String] = mutable.Map()
  private val docCache: mutable.Map[String, Document] = mutable.Map()
  //TODO allow multiple uses of docCache

  private def getDoc(url: String): Option[Document] = {
    if (docCache contains url) {
      docCache remove url
    } else {
      //println(s"fetching $url")
      Try(Jsoup.connect(url).execute().parse()).toOption
    }
  }

  private def storeTitle(url: String, doc: Document): Unit = {
    Try(doc.select("title").text()).toOption match {
      case Some(title) => pageTitles(url) = title
      case None => ()
    }
  }


  def fetchPage(url: String): Option[Document] = this.synchronized {
    getDoc(url) match {
      case Some(doc) =>
        if (!(pageTitles contains url)) storeTitle(url, doc)
        Some(doc)
      case None => None
    }
  }

  def getTitle(url: String): Option[String] = this.synchronized {
    if (!(pageTitles contains url)) {
      getDoc(url) match {
        case Some(doc) =>
          docCache(url) = doc
          storeTitle(url, doc)
        case None => ()
      }
    }
    pageTitles.get(url)
  }

}
