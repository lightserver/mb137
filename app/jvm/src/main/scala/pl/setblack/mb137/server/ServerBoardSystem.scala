package pl.setblack.mb137.server

import akka.actor.{ActorSystem, ActorRef}
import pl.setblack.lsa.events.{NodeConnection, NodeMessage, Node}
import pl.setblack.lsa.io.FileStore
import pl.setblack.mb137.data.BoardSystem

class ServerBoardSystem(nodeId: Long) (implicit  system: ActorSystem) extends BoardSystem{



  var nextClientNodeId:Long = 1024*mainNode.id


  def nextClientNode:Long = {
    nextClientNodeId = nextClientNodeId + 1
    nextClientNodeId
  }

  override  def createStorage() = {
     val fileStorePath = system.settings.config.getString("app.file.filesDir")
     new FileStore(fileStorePath)
  }

  override def createMainNode ():Node = {
    println(s"creating node with id:${nodeId}")
    val node = new Node(nodeId)(storage)

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

  def registerActorConnection( subscriber : ActorRef, clientId: Long ) = {
    val connection = mainNode.registerConnection(clientId, new ServerRemoteProtocol(subscriber, clientId.toString))
  }

  def registeredRemoteActor(board: ActorRef, subscriber: ActorRef): Unit = {
    subscriber ! RegisteredNode( "server", board, nodeId)
  }

  def registerToRemoteActor(board: ActorRef, remotePath: String): Unit = {
      val actorRef = system.actorSelection(remotePath)
      actorRef ! NewNode("server", board, nodeId )
  }

  def registerToRemote(theBoard: ActorRef):Unit = {
    val remoteSystems:Seq[String] = scala.collection.JavaConversions.asScalaBuffer(system.settings.config.getStringList("app.node.connectTo")).toSeq
    println("remote systems are:"+ remoteSystems)
    remoteSystems.foreach( remotePath=> registerToRemoteActor(theBoard, remotePath))
  }
}

