package pl.setblack.mb137.server

import akka.actor._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Keep, Source, Sink, Flow}
import  pl.setblack.mb137.data.BoardMessage

import upickle._


trait Board {
  def theFlow(sender: String): Flow[String, BoardMessage, Unit]

  def injectMessage(message: BoardMessage): Unit
}

object Board {
  def create(system: ActorSystem): Board = {

    val boardActor =
      system.actorOf(Props(new Actor {
        var subscribers = Set.empty[ActorRef]

        def receive: Receive = {
          case NewParticipant(name, subscriber) ⇒
            context.watch(subscriber)
            subscribers += subscriber
            println(s"${name} joined")
          case msg: ReceivedMessage    ⇒ dispatch(msg.toMessage)
          case ParticipantLeft(person) ⇒ println(s"{person} left")
          case Terminated(sub)         ⇒ subscribers -= sub // clean up dead subscribers
        }

        def dispatch(msg: BoardMessage): Unit = {
          println("received message:" + msg.toString)
          subscribers.foreach(_ ! msg)
        }
      }))


    def boardInSink(sender: String) = Sink.actorRef[BoardEvent](boardActor, ParticipantLeft(sender))

    new Board {
      def theFlow(sender: String): Flow[String, BoardMessage, Unit] = {
        println("sendler is:"+sender)
        println(s"sendler2 is: ${sender}")
        val in =
          Flow[String]
            .map(ReceivedMessage(sender, _))
            .to(boardInSink(sender))


        val out =
          Source.actorRef[BoardMessage](1, OverflowStrategy.fail)
            .mapMaterializedValue(boardActor ! NewParticipant(sender, _))

        Flow.wrap(in, out)(Keep.none)
      }
      def injectMessage(message: BoardMessage): Unit = boardActor ! message // non-streams interface
    }
  }

  private sealed trait BoardEvent
  private case class NewParticipant(name: String, subscriber: ActorRef) extends BoardEvent
  private case class ParticipantLeft(name: String) extends BoardEvent
  private case class ReceivedMessage(sender: String, message: String) extends BoardEvent {
    def toMessage: BoardMessage = BoardMessage(sender, read[BoardMessage](message).txt)
  }
}
