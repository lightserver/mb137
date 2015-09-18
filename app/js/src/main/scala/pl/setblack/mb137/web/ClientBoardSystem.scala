package pl.setblack.mb137.web

import org.scalajs.dom.raw.{MessageEvent, WebSocket}
import pl.setblack.lsa.events.{DomainListener, NodeMessageTransport, Node}
import pl.setblack.mb137.data.BoardSystem

import upickle.default._

class ClientBoardSystem(
                         nodeId: Long,
                         connection :WebSocket,
                         serverId : Long,
                         backend : BoardBackend) extends BoardSystem {

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
