package pl.setblack.lsa.events

trait Endpoint {

}

object Local extends Endpoint {

}

object All extends Endpoint {

}

case class Target(val id:Long) extends Endpoint {

}




