package pl.setblack.lsa.events


import upickle.default._
sealed trait ControlEvent {

}

case class RegisteredClient(val clientId :Long, val senderNodeId:Long) extends ControlEvent


object ControlEvent {
  def parseControlEvent(ev : String ): ControlEvent =  {
      read[ControlEvent](ev)
  }
}