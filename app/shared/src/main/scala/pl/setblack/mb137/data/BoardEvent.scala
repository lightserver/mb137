package pl.setblack.mb137.data


sealed trait  BoardEvent
case class NewPost(val message: String, val author: String) extends BoardEvent
