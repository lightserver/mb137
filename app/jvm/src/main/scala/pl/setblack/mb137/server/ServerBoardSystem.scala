package pl.setblack.mb137.server

import akka.actor.ActorRef
import pl.setblack.lsa.events.{NodeMessage, Node}
import pl.setblack.lsa.io.FileStore
import pl.setblack.mb137.data.BoardSystem

class ServerBoardSystem(nodeId: Long)  extends BoardSystem{


  var nextClientNodeId:Long = 1024*mainNode.id


  def nextClientNode:Long = {
    nextClientNodeId = nextClientNodeId + 1
    nextClientNodeId
  }

  override  def createStorage() = {
     new FileStore()
  }

  override def createMainNode ():Node = {
    val node = new Node(nodeId)

    node
  }

  def receiveMessage( msg : NodeMessage): Unit = {
    mainNode.receiveMessage(msg)
  }

  def registerConnection( subscriber : ActorRef, clientId: Long ) = {
    val connection = mainNode.registerConnection(clientId, new ServerWSProtocol(subscriber))
    val controlMessage = mainNode.createClientIdMessage(clientId)
    connection.send(controlMessage)
  }
}

