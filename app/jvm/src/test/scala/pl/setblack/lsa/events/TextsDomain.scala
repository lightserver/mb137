package pl.setblack.lsa.events

import scala.collection.mutable.ArrayBuffer

/**
  * Created by jarek on 9/15/15.
  */
class TextsDomain extends Domain[ArrayBuffer[String]](new ArrayBuffer[String]()){
   def processDomain[T]( event : Event[T]) = {
       event.content match  {
         case x:String => domainObject += x

       }
   }
 }
