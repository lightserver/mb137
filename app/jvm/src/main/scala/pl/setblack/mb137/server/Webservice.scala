package pl.setblack.mb137.server


import java.util.Date

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.stream.stage._
import pl.setblack.mb137.data.BoardMessage

import scala.concurrent.duration._

import akka.http.scaladsl.server.Directives
import akka.stream.Materializer
import akka.stream.scaladsl.Flow

import upickle._

class Webservice(implicit fm: Materializer, system: ActorSystem) extends Directives {

  val theBoard = Board.create(system)

  def route =
    get {
      pathSingleSlash {
        getFromResource("web/index.html")
      } ~
        // Scala-JS puts them in the root of the resource directory per default,
        // so that's where we pick them up
        pathPrefix("scjs"   )(getFromResourceDirectory("")) ~
        path("board") {
         parameter('name) { name ⇒
           handleWebsocketMessages(websocketMessagesFlow(sender = name))
         }
       }
    } ~
      getFromResourceDirectory("web")


  def websocketMessagesFlow(sender: String) : Flow[Message, Message, Unit] =
    Flow[Message]
      .collect {
      case TextMessage.Strict(msg) => msg
    }.via(theBoard.theFlow(sender))
  .map {
      case m @ BoardMessage(sender, message) ⇒ {
        TextMessage.Strict(write(m)) // ... pack outgoing messages into WS JSON messages ...
      }
    }     .via(reportErrorsFlow)


 def reportErrorsFlow[T]: Flow[T, T, Unit] =
    Flow[T]
      .transform(() ⇒ new PushStage[T, T] {
      def onPush(elem: T, ctx: Context[T]): SyncDirective = ctx.push(elem)

      override def onUpstreamFailure(cause: Throwable, ctx: Context[T]): TerminationDirective = {
        println(s"WS stream failed with $cause")
        super.onUpstreamFailure(cause, ctx)
      }
    })
}