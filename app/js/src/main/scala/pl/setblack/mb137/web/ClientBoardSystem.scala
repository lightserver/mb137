package pl.setblack.mb137.web

import org.scalajs.dom.raw.{MessageEvent, WebSocket}
import pl.setblack.lsa.events.{NodeMessageTransport, Node}
import pl.setblack.mb137.data.BoardSystem

import upickle.default._

class ClientBoardSystem(nodeId: Long, connection :WebSocket, serverId : Long) extends BoardSystem{
  var enteredText : String = ""
  override def createMainNode ():Node = {
    val node = new Node(nodeId)

    node.registerConnection( serverId, new ClientWSProtocol(connection))
    connection.onmessage = { (event : MessageEvent) =>
          val msg = read[NodeMessageTransport](event.data.toString).toNodeMessage
          node.receiveMessage(msg)
    }
    node
  }
}
