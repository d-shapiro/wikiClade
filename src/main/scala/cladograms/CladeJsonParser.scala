package cladograms

import net.liftweb.json
import net.liftweb.json.JsonAST.{JArray, JField, JObject, JString}


/**
  * Created by Daniel on 4/7/2019.
  */
object CladeJsonParser {

  def parseString(str: String): Map[String, List[String]] = {
    def fieldToPair(field: JField): (String, List[String]) = {
      field match {
        case JField(key, JArray(arr)) => (key, arr collect {case JString(s) => s})
        case _ => throw new IllegalArgumentException("Unexpected json format")
      }
    }
    val jmap = json.parse(str)
    jmap match {
      case JObject(fields) => (for {
        field <- fields
      } yield fieldToPair(field)).toMap
      case _ => throw new IllegalArgumentException("Unexpected json format")
    }
  }

  def construct(name: String, data: Map[String, List[String]]): Clade = {
    var known: Map[String, Clade] = Map()

    def consHelper(name: String): Clade = {
      if (known contains name) known(name)
      else {
        val cl =
          if (data contains name) new SimpleClade(name, (data(name) map consHelper).reverse)
          else new SimpleClade(name, List())
        known += (name -> cl)
        cl
      }
    }
    consHelper(name)
  }

  def parse(name: String, dataString: String) = construct(name, parseString(dataString))

}
