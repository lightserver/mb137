package pl.setblack.mb137.server

import akka.actor.ActorRef
import pl.setblack.lsa.events.{NodeMessage, Node}
import pl.setblack.mb137.data.BoardSystem

class ServerBoardSystem(nodeId: Long)  extends BoardSystem{


  var nextClientNodeId:Long = 1024*mainNode.id


  def nextClientNode:Long = {
    nextClientNodeId = nextClientNodeId + 1
    nextClientNodeId
  }

  override def createMainNode ():Node = {
    new Node(nodeId)
  }

  def receiveMessage( msg : NodeMessage): Unit = {
    mainNode.receiveMessage(msg)
  }

  def registerConnection( subscriber : ActorRef, clientId: Long ) = {
    mainNode.registerConnection(clientId, new ServerWSProtocol(subscriber))
  }
}

object ServerBoardSystem {
  
}
