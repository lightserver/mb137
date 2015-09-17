package pl.setblack.mb137.data

import pl.setblack.lsa.events.{Event, Domain}

import upickle._

class BoardDomain( topic :String) extends  Domain[BoardMutable]( new BoardMutable(topic)){
  override def processDomain(event: Event): Unit = {
      val boardMessage = read[BoardMessage](event.content)
       domainObject .append(boardMessage)

  }
}
