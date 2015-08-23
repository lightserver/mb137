package pl.setblack.mb137.server


import java.util.Date

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.stream.stage._

import scala.concurrent.duration._

import akka.http.scaladsl.server.Directives
import akka.stream.Materializer
import akka.stream.scaladsl.Flow

import  upickle.default._

class Webservice(implicit fm: Materializer, system: ActorSystem) extends Directives {
  val theChat = Pinger.create(system)

  import system.dispatcher

  system.scheduler.schedule(15.second, 15.second) {
    theChat.injectMessage(PingMessage(s"Bling! The time is ${new Date().toString}.","nicx"))
  }

  def route =
    get {
      pathSingleSlash {
        getFromResource("web/index.html")
      } ~
        // Scala-JS puts them in the root of the resource directory per default,
        // so that's where we pick them up
        path("scjs" / "app-jsdeps.js")(getFromResource("app-jsdeps.js")) ~
        path("scjs" / "app-fastopt.js")(getFromResource("app-fastopt.js")) ~
        pathPrefix("scjs"   )(getFromResourceDirectory("")) ~
        path("chat") {
          parameter('name) { name ⇒
            handleWebsocketMessages(websocketChatFlow(sender = name))
          }
        }
    } ~ getFromResourceDirectory("web")

  def websocketChatFlow(sender: String): Flow[Message, Message, Unit] =
    Flow[Message]
      .collect {
      case TextMessage.Strict(msg) ⇒ msg // unpack incoming WS text messages...
      // This will lose (ignore) messages not received in one chunk (which is
      // unlikely because chat messages are small) but absolutely possible
      // FIXME: We need to handle TextMessage.Streamed as well.
    }
      .via(theChat.pingFlow(sender)) // ... and route them through the chatFlow ...
      .map {
      case c @ PingMessage( message,nic) ⇒ {
        TextMessage.Strict(write(c)) // ... pack outgoing messages into WS JSON messages ...
      }
    }
      .via(reportErrorsFlow) // ... then log any processing errors on stdin

  def reportErrorsFlow[T]: Flow[T, T, Unit] =
    Flow[T]
      .transform(() ⇒ new PushStage[T, T] {
      def onPush(elem: T, ctx: Context[T]): SyncDirective = ctx.push(elem)

      override def onUpstreamFailure(cause: Throwable, ctx: Context[T]): TerminationDirective = {
        println(s"WS stream failed with $cause")
        cause.printStackTrace()
        super.onUpstreamFailure(cause, ctx)
      }
    })
}