package pl.setblack.mb137.data

import pl.setblack.lsa.events._
import pl.setblack.lsa.io.Storage

import upickle.default._

class BoardDomain( topic :String, path: Seq[String]) extends  Domain[BoardMutable](new BoardMutable("default"), path){
  type EVENT = BoardEvent
  override def processDomain(state: BoardMutable, boardEv: BoardEvent,  ctx: EventContext) = {

      println("event in board domain....")
      boardEv match {
        case NewPost(msg, author, timestamp, uuid) =>
          state.append( BoardMessage(author, msg, timestamp,uuid))
        case DeletePost(uuid) =>
          state.delete(uuid)
      }
    DefaultResponse
  }

  override def getEventConverter: EventConverter[EVENT] = BoardEvent
}
