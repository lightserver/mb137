package pl.setblack.mb137.data


case class BoardState(val messages: Seq[BoardMessage], val subject: String) {
  def this() = this( Seq(), "default")

 }