package pl.setblack.moment

import scala.scalajs.js
import scala.scalajs
import scala.scalajs.js.annotation.JSName

@js.native
trait Moment  extends js.Object{
    def fromNow() : String = js.native
}
@JSName("moment")
@js.native
object Moment extends js.Object {
  def apply() : Moment = js.native
  def apply(time: Double) : Moment = js.native
}
