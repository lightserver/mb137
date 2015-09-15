package pl.setblack.lsa.events

case class Address(val target: Endpoint = All, val path:Seq[String]) {

}
