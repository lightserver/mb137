package pl.setblack.mb137.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import pl.setblack.lsa.server.JVMNexus
import pl.setblack.mb137.data.BoardSystem

import scala.util.{Failure, Success}

object Boot extends App {
  implicit val system = ActorSystem()

  implicit val materializer = ActorMaterializer()

   val mainNode = new JVMNexus().start

   val sys = new BoardSystem(mainNode)

  }