package pl.setblack.mb137.server

import akka.actor.{ActorSystem, ActorRef}
import pl.setblack.lsa.concurrency.NoConcurrencySystem
import pl.setblack.lsa.events.{ConnectionData, NodeConnection, NodeMessage, Node}
import pl.setblack.lsa.io.FileStore
import pl.setblack.mb137.data.BoardSystem

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Promise}

class ServerBoardSystem(nodeId: Long) (implicit  system: ActorSystem) extends BoardSystem{
  import ExecutionContext.Implicits.global
  var nextClientNodeId:Long = 2048*nodeId + scala.util.Random.nextInt(1024)
  val connectionData = mutable.Map[Long, ConnectionData]()

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

    val node = new Node(Promise[Long].success(nodeId).future)(storage, new NoConcurrencySystem)

    node
  }

  def receiveMessage( msg : NodeMessage): Unit = {
    mainNode.receiveMessage(msg,
      this.connectionData.getOrElseUpdate( msg.event.sender,  new ConnectionData()))
  }

  def registerConnection( subscriber : ActorRef, clientId: Long ) = {
    val connection = mainNode.registerConnection(clientId, new ServerWSProtocol(subscriber))
    val controlMessage = mainNode.createClientIdMessage(clientId)
    for {
      vc <- connection
      msg <-controlMessage
    } yield vc.send(msg)

  }

  def registerActorConnection( subscriber : ActorRef, clientId: Long ) = {
    val connection = mainNode.registerConnection(clientId, new ServerRemoteProtocol(subscriber, clientId.toString))
    resync()
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

