name := "wikiClade"

version := "1.0.7"

scalaVersion := "2.12.8"

resolvers += Resolver.jcenterRepo
resolvers += DefaultMavenRepository

libraryDependencies += "org.jsoup" % "jsoup" % "1.11.3"
libraryDependencies += "guru.nidi" % "graphviz-java" % "0.8.3"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.26"
