package pl.setblack.lsa.events


abstract class Domain[O](val domainObject:O) {
   var recentEvents  = Map[Long,Seq[Long]]()

    def receiveEvent (event :Event) = {
        recentEvents = recentEvents + ( event.sender -> (
            recentEvents.getOrElse(event.sender, Seq() ) :+ event.id ) )
        processDomain(event)
    }

   def processDomain( event : Event)

}
