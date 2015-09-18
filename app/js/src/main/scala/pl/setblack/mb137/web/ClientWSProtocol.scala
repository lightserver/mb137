package pl.setblack.mb137.web

import org.scalajs.dom.raw.WebSocket
import pl.setblack.lsa.events.{NodeMessageTransport, NodeMessage, Protocol}

import upickle.default._

class ClientWSProtocol(val connection : WebSocket) extends Protocol{
  override def send(msg: NodeMessage): Unit =  {
        connection.send(  write[NodeMessageTransport](msg.toTransport))
  }
}
