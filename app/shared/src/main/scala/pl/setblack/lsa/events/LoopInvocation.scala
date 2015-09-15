package pl.setblack.lsa.events

class LoopInvocation(val target:Node) extends Protocol {

  override def send[T](msg: Message[T]): Unit = {
    target.receiveMessage(msg)
  }
}
