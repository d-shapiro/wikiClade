package cladograms

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.collection.mutable
import scala.util.Try

object WikiProxy {

  private val pageTitles: mutable.Map[String, Option[String]] = mutable.Map()

  private def getDoc(url: String): Option[Document] = {
      Try(Jsoup.connect(url).execute().parse()).toOption
  }

  private def storeTitle(url: String, doc: Document): Unit = {
    pageTitles(url) = Try(doc.select("title").text()).toOption
  }


  def fetchPage(url: String): Option[Document] = this.synchronized {
    getDoc(url) match {
      case Some(doc) =>
        if (!(pageTitles contains url)) storeTitle(url, doc)
        Some(doc)
      case None =>
        pageTitles(url) = None
        None
    }
  }

  def getTitle(url: String): Option[String] = this.synchronized {
    if (!(pageTitles contains url)) {
      getDoc(url) match {
        case Some(doc) =>
          storeTitle(url, doc)
        case None =>
          pageTitles(url) = None
      }
    }
    pageTitles(url)
  }

}
