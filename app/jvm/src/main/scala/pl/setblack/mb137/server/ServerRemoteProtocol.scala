package pl.setblack.mb137.server

import akka.actor.{ActorRef, ActorSystem}
import pl.setblack.lsa.events.{NodeMessageTransport, NodeMessage, Protocol}
import upickle.default._

class ServerRemoteProtocol(val remote : ActorRef, val senderId: String)( implicit  system: ActorSystem )  extends Protocol {


  override def send(msg: NodeMessage): Unit = {
      remote ! ReceivedMessage(senderId,  write[NodeMessageTransport](msg.toTransport))
  }
}
