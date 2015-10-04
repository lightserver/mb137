package pl.setblack.lsa.events

import pl.setblack.lsa.io.Storage
import upickle.default._

import scala.collection.mutable.ArrayBuffer

abstract class Domain[O](var domainObject: O, val path: Seq[String]) {
  var recentEvents = Map[Long, Seq[Long]]()
  var listeners = Seq[DomainListener[O]]()
  val eventsHistory = scala.collection.mutable.ArrayBuffer.empty[Event]

  def seenEvent(event: Event ) : Boolean = {
    recentEvents.get(event.sender).getOrElse(Seq()).contains( event.id)
  }

  def resendEvents(clientId: Long, recentEvents: Map[Long, Seq[Long]]): Seq[Event] = {
    this.eventsHistory
  }

  def receiveEvent(event: Event):Boolean = {
    if (!seenEvent(event)) {
       println("received event : " + event.id +" from : " + event.sender )
      recentEvents = recentEvents + (event.sender -> (
        recentEvents.getOrElse(event.sender, Seq()) :+ event.id))
      println ("contains:" + seenEvent(event))
      eventsHistory += event
      processDomain(event)
      listeners.foreach(l => l.onDomainChanged(domainObject, event))
      true
    } else {
      false
    }
  }

  def registerListener(listener: DomainListener[O]): Unit = {
    println("registering listener")
    listeners = listeners :+ listener
  }

  /*def restoreDomain(events: Seq[Event]): Unit = {
    println("events is : " +events )
    events.foreach(e => receiveEvent(e))
  }*/

  def processDomain(event: Event)
}
