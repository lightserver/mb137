package pl.setblack.mb137.web

import pl.setblack.lsa.io.{ClientWSProtocol, Storage, WebStorage}
import org.scalajs.dom.raw.{MessageEvent, WebSocket}
import pl.setblack.lsa.events.{ConnectionData, DomainListener, NodeMessageTransport, Node}
import pl.setblack.mb137.data.BoardSystem

import upickle.default._

import scala.collection.mutable

class ClientBoardSystem(
                         nodeId: Long,
                         connection :WebSocket,
                         serverId : Long
                         ) extends BoardSystem {

  val connectionData = mutable.Map[Long, ConnectionData]()

  override def createStorage(): Storage = {
    new WebStorage()
  }

  override def createMainNode ():Node = {
    val node = new Node(nodeId)(storage)

    node.registerConnection( serverId, new ClientWSProtocol(connection, node))
    connection.onmessage = { (event : MessageEvent) =>
          val msg = read[NodeMessageTransport](event.data.toString).toNodeMessage
          node.receiveMessage(msg ,connectionData.getOrElseUpdate(serverId, new ConnectionData()))
    }

    node
  }


}
