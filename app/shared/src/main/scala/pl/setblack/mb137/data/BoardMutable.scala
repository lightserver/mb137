package pl.setblack.mb137.data

import scala.collection.mutable.ArrayBuffer

class BoardMutable(val subject:String) {
  val messages = ArrayBuffer[BoardMessage]()

  def append( msg : BoardMessage ) = {
    this.messages += msg
  }
}
