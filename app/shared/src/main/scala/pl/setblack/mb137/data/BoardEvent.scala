package pl.setblack.mb137.data

import pl.setblack.lsa.events.EventConverter
import upickle.Invalid
import upickle.default._

sealed trait  BoardEvent  {

}


case class NewPost(val message: String, val author: String, timestamp : Long,  uuid :String) extends BoardEvent {

}

case class DeletePost(val uuid: String) extends BoardEvent {

}

object BoardEvent extends EventConverter[BoardEvent]{

   override def readEvent(str: String): BoardEvent = {
      try {
         read[BoardEvent](str)
      } catch {
         case inv : Invalid => {
            println(s"unrecognized ${str}")
            throw new RuntimeException(inv)
         }
      }
   }

   override def writeEvent(e: BoardEvent): String = {
      write[BoardEvent](e)
   }


}
