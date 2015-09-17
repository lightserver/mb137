package pl.setblack.mb137.server

import akka.actor.ActorRef
import pl.setblack.lsa.events.{NodeMessage, Protocol, NodeConnection}

class ServerWSProtocol(val subcriber: ActorRef) extends  Protocol{
  override def send(msg: NodeMessage): Unit = {
      subcriber ! msg
  }
}
