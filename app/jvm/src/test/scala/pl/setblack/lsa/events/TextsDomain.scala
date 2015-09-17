package pl.setblack.lsa.events

import scala.collection.mutable.ArrayBuffer

class TextsDomain extends Domain[ArrayBuffer[String]](new ArrayBuffer[String]()){
   def processDomain( event : Event) = {
       event.content match  {
         case x:String => domainObject += x

       }
   }
 }
