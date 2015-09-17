package pl.setblack.lsa.events

/**
 * Node represents system to register domains and send pl.setblack.lsa.events.
 */
class Node(val id: Long) {
  private var connections: Map[Long, NodeConnection] = Map()
  private var domains: Map[Seq[String], Domain[_]] = Map()
  private var messageListeners: Seq[MessageListener] = Seq()
  private var loopConnection = registerConnection(id, new LoopInvocation(this))



  def registerMessageListener(listener: MessageListener): Unit = {
    messageListeners = messageListeners :+ listener
  }

  def registerDomain[O](path: Seq[String], domain: Domain[O]) = {
    domains = domains + (path -> domain)
  }

  private[events] def hasDomain(path: Seq[String]): Boolean = {
    domains contains (path)
  }

  /**
   * Dispatch event from this Node to ... other Node (or not).
   */
  def sendEvent(content: String, adr: Address) = {
    val event = new Event(content, 0, id)
    val message = new NodeMessage(adr, event)
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


  def registerConnection(id: Long, protocol: Protocol) = {
    val connection = new NodeConnection(id, protocol)
    this.connections = this.connections + (id -> connection)
    connection
  }

  def getConnections(): Map[Long, NodeConnection] = {
    this.connections
  }


  /**
   * Node receives message here.
   */
   def receiveMessage(msg: NodeMessage) = {
    messageListeners foreach (listener => listener.onMessage(msg))
    this.domains
      .filter( (v) => msg.destination.path.startsWith(v._1))
      .foreach( (v) => v._2.receiveEvent(msg.event))
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

}