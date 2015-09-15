package pl.setblack.mb137.data

import scala.collection.mutable.ArrayBuffer

/**
 * Created by jarek on 9/15/15.
 */
class BoardMutable(val subject:String) {
  val messages = ArrayBuffer[BoardMessage]()

  def append( msg : BoardMessage ) = {
    this.messages += msg
  }
}
