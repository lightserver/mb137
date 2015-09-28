package pl.setblack.lsa.events


import upickle.default._

abstract class Domain[O](var domainObject: O) {
  var recentEvents = Map[Long, Seq[Long]]()
  var listeners = Seq[DomainListener[O]]()

  val eventsHistory = scala.collection.mutable.ArrayBuffer.empty[Event]

  def seenEvent(event: Event ) : Boolean = {
    recentEvents.get(event.sender).getOrElse(Seq()).contains( event.id)
  }


  def receiveEvent(event: Event) = {
    if (!seenEvent(event)) {
       println("received event : " + event.id +" from : " + event.sender )
      recentEvents = recentEvents + (event.sender -> (
        recentEvents.getOrElse(event.sender, Seq()) :+ event.id))
      println ("contains:" + seenEvent(event))
      eventsHistory += event
      processDomain(event)
      listeners.foreach(l => l.onDomainChanged(domainObject, event))
    }


  }

  def registerListener(listener: DomainListener[O]): Unit = {
    println("registering listener")
    listeners = listeners :+ listener
  }

  def restoreDomain(events: Seq[Event]): Unit = {
    println("events is : " +events )
    events.foreach(e => receiveEvent(e))
  }

  def processDomain(event: Event)


}
