package pl.setblack.mb137.server

import akka.actor.Actor.Receive
import akka.actor._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Source, Keep, Sink, Flow}


case class PingMessage( val txt: String, val nic:String)

trait Pinger {
  def pingFlow(sender: String): Flow[String, PingMessage, Unit]

  def injectMessage(message: PingMessage): Unit
}


object Pinger {
  def create(system: ActorSystem): Pinger = {

    val chatActor =
      system.actorOf(Props(new Actor {
        var subscribers = Set.empty[ActorRef]

        def receive: Receive = {
          case NewParticipant(name, subscriber) ⇒
            context.watch(subscriber)
            subscribers += subscriber
            sendAdminMessage(s"$name joined!")
          case msg: ReceivedMessage    ⇒ dispatch(msg.toChatMessage)
          case ParticipantLeft(person) ⇒ sendAdminMessage(s"$person left!")
          case Terminated(sub)         ⇒ subscribers -= sub // clean up dead subscribers
        }
        def sendAdminMessage(msg: String): Unit = dispatch(PingMessage( "uu:"+ msg,"niccc"))
        def dispatch(msg: PingMessage): Unit = subscribers.foreach(_ ! msg)
      }))

    // Wraps the chatActor in a sink. When the stream to this sink will be completed
    // it sends the `ParticipantLeft` message to the chatActor.
    // FIXME: here some rate-limiting should be applied to prevent single users flooding the chat
    def chatInSink(sender: String) = Sink.actorRef[PingEvent](chatActor, ParticipantLeft(sender))

    new Pinger {
      def pingFlow(sender: String): Flow[String, PingMessage, Unit] = {
        val in =
          Flow[String]
            .map(ReceivedMessage(sender, _))
            .to(chatInSink(sender))

        // The counter-part which is a source that will create a target ActorRef per
        // materialization where the chatActor will send its messages to.
        // This source will only buffer one element and will fail if the client doesn't read
        // messages fast enough.
        val out =
          Source.actorRef[PingMessage](1, OverflowStrategy.fail)
            .mapMaterializedValue(chatActor ! NewParticipant(sender, _))

        Flow.wrap(in, out)(Keep.none)
      }
      def injectMessage(message: PingMessage): Unit = chatActor ! message // non-streams interface
    }
  }

  private sealed trait PingEvent
  private case class NewParticipant(name: String, subscriber: ActorRef) extends PingEvent
  private case class ParticipantLeft(name: String) extends PingEvent
  private case class ReceivedMessage(sender: String, message: String) extends PingEvent {
    def toChatMessage: PingMessage = PingMessage( message,"")
  }
}