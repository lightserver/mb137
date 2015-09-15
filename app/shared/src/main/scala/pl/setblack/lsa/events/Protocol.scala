package pl.setblack.lsa.events

/**
 * Used to send messages between Nodes.
 */
trait Protocol {
    def send[T] ( msg:Message[T]): Unit
}


