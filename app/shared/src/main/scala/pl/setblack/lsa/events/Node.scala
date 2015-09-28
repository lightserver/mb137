package pl.setblack.lsa.events


import pl.setblack.lsa.io.Storage
import upickle.default._

import scala.collection.mutable.ArrayBuffer

/**
 * Node represents system to register domains and send pl.setblack.lsa.events.
 */
class Node(val id: Long) {
  private var connections: Map[Long, NodeConnection] = Map()
  private var domains: Map[Seq[String], Domain[_]] = Map()
  private var messageListeners: Seq[MessageListener] = Seq()
  private val loopConnection = registerConnection(id, new LoopInvocation(this))

  private var nextEventId:Long = 0

  def saveDomains(storage: Storage)  = {
    this.domains.foreach( kv => storage.save(write[ArrayBuffer[Event]](kv._2.eventsHistory), Seq("start") ++ kv._1 ))
  }

  def loadDomains(storage: Storage) = {

    this.domains.foreach(
      kv => kv._2.restoreDomain(
                read[ArrayBuffer[Event]](
                  storage.load(Seq("start") ++ kv._1 ).getOrElse("[]") )))

  }

  def registerMessageListener(listener: MessageListener): Unit = {
    messageListeners = messageListeners :+ listener
  }

  def registerDomain[O](path: Seq[String], domain: Domain[O]) = {
    domains = domains + (path -> domain)
  }

  private[events] def hasDomain(path: Seq[String]): Boolean = {
    domains contains (path)
  }


  def sendEvent(content: String, domain: Seq[String]): Unit = {
    val adr = Address(All, domain)
    println("sending eventto:" + adr.toString)
    sendEvent(content, adr)
  }

  /**
   * Dispatch event from this Node to ... other Node (or not).
   */
  def sendEvent(content: String, adr: Address): Unit = {
    val event = new Event(content, getNextEventId(), id)
    val message = new NodeMessage(adr, event, Seq(this.id))
    getConnectionsForAddress(adr).foreach(nc => nc.send(message))

  }

  private[events] def getConnectionsForAddress(adr: Address): Seq[NodeConnection] = {
    adr.target match {
      case Local => Seq(this.loopConnection)
      case All => this.connections.values.toSeq
      case System => Seq()
      case Target(x) => this.connections.values.filter(node => node.knows(x)).toSeq
    }
  }

  def createClientIdMessage(clientId: Long): NodeMessage = {
    val event = new Event(write[ControlEvent](RegisteredClient(clientId, this.id)), 1, this.id)
    NodeMessage(Address(System), event)
  }

  def registerConnection(id: Long, protocol: Protocol) = {
    val connection = new NodeConnection(id, protocol)
    this.connections = this.connections + (id -> connection)

    connection
  }

  def getConnections(): Map[Long, NodeConnection] = {
    this.connections
  }

  def registerDomainListener[O](listener: DomainListener[O], path: Seq[String]): Unit = {
    println("registering listener for:" + path.toString)
    println("a domeny to:" + this.domains.keys.toString)
    this.filterDomains(path).foreach(x => x match {
      case d: Domain[O] => {
        println("filtered domain:" + path)
        d.registerListener(listener)
      }
    })
  }

  def processSysMessage(ev: Event): Unit = {
    val ctrlEvent = read[ControlEvent](ev.content)
    ctrlEvent match {
      //does not make any sense now...
      case RegisteredClient(clientId, serverId) => println("registered as: " + id)
    }
  }

  private def reroute(msg: NodeMessage): Unit = {
    val routedMsg = msg.copy(route = msg.route :+ this.id)
    this.connections.values.filter(p => !routedMsg.route.contains(p.targetId))
      .foreach(nc => {
      println("rerouted by:" + this.id + " to: " + nc.targetId )
      nc.send(routedMsg)
    })
  }


  private def filterDomains(path: Seq[String]): Seq[Domain[_]] = {
    this.domains
      .filter((v) => path.startsWith(v._1)).values.toSeq
  }

  /**
   * Node receives message here.
   */
  def receiveMessage(msg: NodeMessage) = {
    receiveMessageLocal(msg)
    reroute(msg)
  }

  def receiveMessageLocal(msg: NodeMessage) = {
    messageListeners foreach (listener => listener.onMessage(msg))
    if (msg.destination.target == System) {
      processSysMessage(msg.event)
    } else {
      println("message processed by domains")
      filterDomains(msg.destination.path).foreach((v) => v.receiveEvent(msg.event))
    }
  }

  /**
   * Send event to local domains.
   *
   */
  private def receiveLocalEvent[T](t: T, adr: Address): Unit = {

  }

  private def getLocalLoopConnection(): NodeConnection = {
    this.loopConnection
  }

  def getDomainObject(path: Seq[String]) =  {
      domains.get(path).get.domainObject
  }

  def getNextEventId () : Long = {
    this.nextEventId += 1
    this.nextEventId
  }
}