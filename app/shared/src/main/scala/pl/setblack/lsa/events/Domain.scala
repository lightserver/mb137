package pl.setblack.lsa.events

/**
 * Created by jarek on 6/28/15.
 */
abstract class Domain[O](val domainObject:O) {
   var recentEvents  = Map[Long,Seq[Long]]()

    def receiveEvent[T] (event :Event[T]) = {
        recentEvents = recentEvents + ( event.sender -> (
            recentEvents.getOrElse(event.sender, Seq() ) :+ event.id ) )
        processDomain(event)
    }

   def processDomain[T]( event : Event[T])

}
