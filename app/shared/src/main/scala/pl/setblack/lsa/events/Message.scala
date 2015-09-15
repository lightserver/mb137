package pl.setblack.lsa.events

/**
 *  Message is only known by Nodes.
 *
 */
class Message[T](
                  val destination: Address,
                  val event: Event[T],
                  val route : Seq[Long] = Seq()) {
}
