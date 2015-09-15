package pl.setblack.mb137.data


case class SysMessageId( val id:Long, val sender : Long)

case class SysMessage(val id: SysMessageId, val data : BoardEvent)
