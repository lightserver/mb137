package pl.setblack.mb137.data


case class SysState (val myId  : Long,
  val board : BoardState,
  val recentMessages  :  Seq[ SysMessageId] ) {
  def receiveLocal(ev : BoardEvent): SysState = ev match  {
    case p : NewPost => this.copy( board = board.copy(messages = board.messages :+ new BoardMessage(p.message, p.author)))
    case _ => this
  }

  def this( myId : Long) =  this(myId, new BoardState(), Seq() )

  def receiveExternal(sysEv : SysMessage): SysResult= {
      if (!recentMessages.contains(sysEv.id)) {
       SysResult ( this.copy( recentMessages = this.recentMessages :+ sysEv.id).receiveLocal(sysEv.data), true)
      } else {
        SysResult (this, false)
      }
  }

}
