package pl.setblack.mb137.data

import pl.setblack.lsa.events.{Event, Domain}


class BoardDomain( topic :String) extends  Domain[BoardMutable]( new BoardMutable(topic)){
  override def processDomain[T](event: Event[T]): Unit = {
    event.content match {
      case x:BoardMessage => domainObject .append(x)
    }
  }
}
