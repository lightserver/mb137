package pl.setblack.lsa.events


import upickle.default._

abstract class Domain[O](var domainObject: O) {
  var recentEvents = Map[Long, Seq[Long]]()
  var listeners = Seq[DomainListener[O]]()

  val eventsHistory  = scala.collection.mutable.ArrayBuffer.empty[Event]

  def receiveEvent(event: Event) = {
    recentEvents = recentEvents + (event.sender -> (
      recentEvents.getOrElse(event.sender, Seq()) :+ event.id))
    eventsHistory += event
    processDomain(event)
    listeners.foreach(l => l.onDomainChanged(domainObject, event))
    //dom.localStorage.setItem("recent_events", write(recentEvents))
  }

  def registerListener(listener: DomainListener[O]): Unit = {
    println("registering listener")
    listeners = listeners :+ listener
  }

  def restoreDomain(events : Seq[Event]): Unit = {
      events.foreach( e => processDomain(e))
  }

  def processDomain(event: Event)



}
