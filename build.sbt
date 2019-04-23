name := "wikiClade"

version := "1.0.2"

scalaVersion := "2.12.8"

resolvers += Resolver.jcenterRepo
resolvers += DefaultMavenRepository

//libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"
//libraryDependencies += "net.liftweb" %% "lift-json" % "3.3.0"
//libraryDependencies += "fastily" %  "jwiki" % "1.7.0"
//libraryDependencies += "com.bitplan" % "mediawiki-japi" % "0.1.03"
//libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "2.1.0"
libraryDependencies += "org.jsoup" % "jsoup" % "1.11.3"
libraryDependencies += "guru.nidi" % "graphviz-java" % "0.8.3"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.26"
