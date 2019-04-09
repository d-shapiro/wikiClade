import cladograms._

val jsonstr = "{\n\t\"Euphorbia Milii\":\n\t[\n\t\t\"Plantae\",\n\t\t\"Angiosperms\",\n\t\t\"Eudicots\",\n\t\t\"Rosids\",\n\t\t\"Malpighiales\",\n\t\t\"Euphorbiaceae\",\n\t\t\"Euphorbia\"\n\t],\n\n\t\"Euphorbia\": [\n\t \t\"Plantae\",\n\t\t\"Angiosperms\",\n\t\t\"Eudicots\",\n\t\t\"Rosids\",\n\t\t\"Malpighiales\",\n\t\t\"Euphorbiaceae\",\n\t\t\"Euphorbioideae\",\n\t\t\"Euphorbieae\",\n\t\t\"Euphorbiinae\"],\n\n\t\"Euphorbieae\": [\n\t\t\"Plantae\",\n\t\t\"Angiosperms\",\n\t\t\"Eudicots\",\n\t\t\"Rosids\",\n\t\t\"Malpighiales\",\n\t\t\"Euphorbiaceae\",\n\t\t\"Euphorbioideae\"],\n\n\t\"Euphorbioideae\": [\n\t\t\"Plantae\",\n\t\t\"Angiosperms\",\n\t\t\"Eudicots\",\n\t\t\"Rosids\",\n\t\t\"Malpighiales\",\n\t \t\"Euphorbiaceae\"],\n\n\t\"Euphorbiaceae\": [\n\t\t\"Plantae\",\n\t\t\"Angiosperms\",\n\t\t\"Eudicots\",\n\t\t\"Rosids\",\n\t\t\"Malpighiales\"],\n\n\t\"Malpighiales\": [\n\t\t\"Plantae\",\n\t\t\"Angiosperms\",\n\t\t\"Eudicots\",\n\t\t\"Rosids\",\n\t\t\"Fabids\"],\n\n\t\"Rosids\": [\n\t\t\"Plantae\",\n\t\t\"Angiosperms\",\n\t\t\"Eudicots\",\n\t\t\"Superrosids\"],\n\n\t\"Superrosids\": [\n\t\t\"Plantae\",\n\t\t\"Angiosperms\",\n\t\t\"Eudicots\",\n\t\t\"Core eudicots\"],\n\n\t\"Eudicots\": [\n\t\t\"Plantae\",\n\t\t\"Angiosperms\"],\n\n\t\"Angiosperms\": [\n\t \t\"Plantae\",\n\t\t\"Spermatophytes\"],\n\n\t\"Spermatophytes\": [\n\t\t\"Plantae\",\n\t\t\"Tracheophytes\"],\n\n\t\"Tracheophytes\": [\n\t\t\"Plantae\",\n\t\t\"Embryophytes\",\n\t\t\"Polysporangiophytes\"],\n\n\t\"Polysporangiophytes\": [\n\t\t\"Plantae\",\n\t\t\"Embryophytes\"],\n\n\t\"Embryophytes\": [\n\t\t\"Plantae\"]\n}\n"
val milii = CladeJsonParser.parse("Euphorbia Milii", jsonstr)
val malp = milii.ancestors(2)
malp.name
milii.ancestors(0).ancestors(1).ancestors(0).ancestors(0).ancestors(0) == malp
def cladogramToString(cladogram: Cladogram): String = {
  val children = cladogram.children.toList
  children match {
    case Nil => cladogram.clade.name
    case ch :: Nil => cladogram.clade.name + "\n" + cladogramToString(ch)
    case _ => cladogram.clade.name + "\n" + (for {
      ch <- children
    } yield cladogramToString(ch)).toString()
  }
}


val cladSet = Cladogram.construct(List(malp, milii))
cladSet.size
cladogramToString(cladSet.toList.head)

//val jsonstr2 = "{\n\t\"Falconeria insignis\" : [\n\t\t\"Plantae\",\n\t\t\"Angiosperms\",\n\t\t\"Eudicots\",\n\t\t\"Rosids\",\n\t\t\"Malpighiales\",\n\t\t\"Euphorbiaceae\",\n\t\t\"Euphorbioideae\",\n\t\t\"Hippomaneae\",\n\t\t\"Hippomaninae\",\n\t\t\"Falconeria\"\n\t],\n\n\t\"Hippomaninae\": [\n\t\t\"Plantae\",\n\t\t\"Angiosperms\",\n\t\t\"Eudicots\",\n\t\t\"Rosids\",\n\t\t\"Malpighiales\",\n\t \t\"Euphorbiaceae\",\n\t\t\"Euphorbioideae\",\n\t\t\"Hippomaneae\"\n\t],\n\n\t\"Hippomaneae\": [\n\t\t\"Plantae\",\n\t\t\"Angiosperms\",\n\t\t\"Eudicots\",\n\t\t\"Rosids\",\n\t\t\"Malpighiales\",\n\t\t\"Euphorbiaceae\",\n\t\t\"Euphorbioideae\"\n\t]\n}"
//val insig = CladeJsonParser.parse("Falconeria insignis", jsonstr2)

val a = SimpleClade("A", List())
val b = SimpleClade("B", List(a))
val c = SimpleClade("C", List(b, a))
val d1 = SimpleClade("D1", List(b, a))
val e1 = SimpleClade("E1", List(d1, c, a))
val f1 = SimpleClade("F1", List(e1))
val d2 = SimpleClade("D2", List(c, a))
val e2 = SimpleClade("E2", List(d2, a))

val cladSet2 = Cladogram.construct(List(e2, f1))
cladSet2.size
val clada = cladSet2.toList.head
clada.clade.name
clada.children.size == 1
val cladb = clada.children.head
cladb.clade.name
cladb.children.size == 1
val cladc = cladb.children.head
cladc.clade.name
cladc.children.size == 2
val l = cladc.children.toList
cladogramToString(l.head)
cladogramToString(l.tail.head)


