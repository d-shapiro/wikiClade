package cladograms

class WikiTaxoboxClade(val name: String, taxonomyPath: Option[String], priorityOverride: Double = 100) //TODO
  extends WikiClade {

  override def getMeta: WikiCladeMetadata = {

  }

}
