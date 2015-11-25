package pl.setblack.lsa.events



import pl.setblack.lsa.io.{DomainStorage, Storage}
import upickle.default._

import scala.collection.mutable.ArrayBuffer

/**
 * Node represents system to register domains and send pl.setblack.lsa.events.
 */
class Node(val id: Long)( implicit val storage :Storage) {



  private var connections: Map[Long, NodeConnection] = Map()
  private var domains: Map[Seq[String], Domain[_]] = Map()
  private var domainStorages : Map[Seq[String], DomainStorage] = Map()
  private var messageListeners: Seq[MessageListener] = Seq()
  private val loopConnection = registerConnection(id, new LoopInvocation(this))

  private var nextEventId:Long = 0

 /* def saveDomains()  = {
    this.domains.foreach( kv => saveDomain(kv._1, kv._2))
  }

  def saveDomain(path: Seq[String], domain: Domain[_]) = {
    storage.save(write[ArrayBuffer[Event]](domain.eventsHistory), Seq("start") ++ path )
  }*/

  def loadDomains() = {
    this.domains.foreach(
      kv => this.domainStorages.get( kv._1).foreach(
        ds => {
            this.nextEventId += ds.loadEvents( kv._2)
           println (s"-----next event id is:" + this.nextEventId)
        }
      )
    )
  }

  def registerMessageListener(listener: MessageListener): Unit = {
    messageListeners = messageListeners :+ listener
  }

  def registerDomain[O](path: Seq[String], domain: Domain[O]) = {
    domains = domains + (path -> domain)
    val domainStore = new DomainStorage(path, storage)
    domainStorages = domainStorages + (path -> domainStore)
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
    this.sendEvent(event, adr)
  }

  private def sendEvent(event:Event, adr: Address): Unit = {
    val message = new NodeMessage(adr, event, Seq(this.id))
    getConnectionsForAddress(adr).foreach(nc => nc.send(message))
  }

  private[events] def getConnectionsForAddress(adr: Address): Seq[NodeConnection] = {
    adr.target match {
      case Local => Seq(this.loopConnection)
      case All => this.connections.values.toSeq
      case System => this.connections.values.toSeq
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
    this.filterDomains(path).foreach(x => x match {
      case d: Domain[_] => {
        println("filtered domain:" + path)
        d.asInstanceOf[Domain[O]].registerListener(listener)
      }
    })
  }

  private def resyncDomain(sync: ResyncDomain): Unit = {
    val address = Address(Target(sync.clientId), sync.domain)

    this.filterDomains(sync.domain).map( domain => {
      domain.resendEvents(sync.clientId, sync.recentEvents).foreach( ev => sendEvent( ev, address))
      if ( sync.syncBack ) {
        val event = Event(write[ControlEvent](ResyncDomain(this.id, sync.domain,  domain.recentEvents, false)),0,this.id)
        val msg =  new NodeMessage(Address(System, sync.domain), event, Seq(this.id))
        this.getConnectionsForAddress(address).foreach( nc => nc.send(msg))
      }
    } )
  }

  def processSysMessage(ev: Event): Unit = {
    val ctrlEvent = read[ControlEvent](ev.content)
    ctrlEvent match {
      //does not make any sense now...
      case RegisteredClient(clientId, serverId) => println("registered as: " + id)
      case sync : ResyncDomain => resyncDomain(sync)
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
    println("received local message")
    messageListeners foreach (listener => listener.onMessage(msg))
    if (msg.destination.target == System) {
      processSysMessage(msg.event)
    } else {
      filterDomains(msg.destination.path).foreach((v) => sendEvenToDomain(msg.event, v))
    }
  }

  def saveEvent(event: Event, path: Seq[String]) = {
    domainStorages.get(path).foreach( store=>store.saveEvent(event))
  }

  private def sendEvenToDomain(event:Event , domain:Domain[_]) = {
    println("passing event to domain:" + domain.path)
    if ( domain.receiveEvent(event)) {
      saveEvent(event, domain.path)
    }
  }

  def getDomainObject(path: Seq[String]) =  {
      domains.get(path).get.getState()
  }

  def getNextEventId () : Long = {
    this.nextEventId += 1
    this.nextEventId
  }

  def resync() = {
    this.domains.foreach(
      kv =>  syncDomain(kv._1,kv._2, true))
  }


  private def syncDomain(path: Seq[String], domain: Domain[_], syncBack : Boolean) ={
      val event = Event(ControlEvent.writeEvent(ResyncDomain(this.id, path,  domain.recentEvents, syncBack)),0,this.id)
      val adr = Address(System, path)
    println("sending event:"+ event)
       this.sendEvent(event,adr)
  }
}
