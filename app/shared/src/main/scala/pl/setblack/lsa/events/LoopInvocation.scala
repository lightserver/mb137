package pl.setblack.lsa.events

class LoopInvocation(val target:Node) extends Protocol {

  override def send(msg: NodeMessage): Unit = {
    target.receiveMessage(msg)
  }
}
