package pl.setblack.mb137.data


case class BoardState(val messages: Seq[BoardMessage],
                      val subject: String,
                      val inputText :String
                      ) {
  def this() = this( Seq(), "default", "")



  def addMessage(msg : BoardMessage ): BoardState = {
    copy( messages =  messages :+ msg)
  }
 }