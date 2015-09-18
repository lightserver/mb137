package pl.setblack.lsa.events


abstract class Domain[O](var domainObject:O) {
   var recentEvents  = Map[Long,Seq[Long]]()
   var listeners  = Seq[DomainListener[O]]()


    def receiveEvent (event :Event) = {
        recentEvents = recentEvents + ( event.sender -> (
            recentEvents.getOrElse(event.sender, Seq() ) :+ event.id ) )
        processDomain(event)
       listeners.foreach( l => l.onDomainChanged(domainObject, event))
    }

    def registerListener(listener: DomainListener[O]): Unit = {
       println("registering listener")
      listeners  = listeners :+ listener
    }

   def processDomain( event : Event)

}
