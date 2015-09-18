package pl.setblack.mb137.data

import pl.setblack.lsa.events.{Event, Domain}

import upickle.default._

class BoardDomain( topic :String) extends  Domain[BoardMutable](new BoardMutable("default")){
  override def processDomain(event: Event): Unit = {
      println("processing board event")
      val boardEv = BoardEvent.readBoardEvent(event.content)
      println("event is " + boardEv.toString)
      boardEv match {
        case NewPost(msg, author) =>
          domainObject.append( BoardMessage(author, msg))
      }
    println("listeners:" + listeners.toString)
  }
}
