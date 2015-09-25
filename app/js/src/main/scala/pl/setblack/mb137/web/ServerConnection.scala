package pl.setblack.mb137.web

import org.scalajs.dom
import org.scalajs.dom.raw.{WebSocket, Document}
import pl.setblack.lsa.events.{Event, RegisteredClient, ControlEvent, NodeMessageTransport}
import pl.setblack.mb137.data.BoardMessage
import upickle.default._

class ServerConnection(val backend : BoardBackend)  {
  val connection: WebSocket = startWs()
  var system:ClientBoardSystem = null

  def sendBoardMessage( msg: BoardMessage): Unit = {
    connection.send(write(msg))
  }

  private def processSysMessage(ev : Event, connection : WebSocket): Unit = {
    val ctrlEvent = ControlEvent.parseControlEvent(ev.content)
    ctrlEvent match {
      //does not make any sense now...
      case RegisteredClient(id,serverId) => {
        println("registered once as: " + id)

        system = new ClientBoardSystem ( id, connection, serverId, backend)
        system.mainNode.registerDomainListener( backend, Seq("default"))
      }
    }
  }

  private def  startWs() = {
    val connection = new WebSocket(getWebsocketUri(dom.document, "irek"))
    connection.onopen = { (event: org.scalajs.dom.raw.Event) ⇒
      println("connection done");

      event
    }
    connection.onerror = { (event: org.scalajs.dom.raw.ErrorEvent) ⇒

    }
    connection.onmessage = { (event: org.scalajs.dom.raw.MessageEvent) ⇒
       val msg = read[NodeMessageTransport](event.data.toString).toNodeMessage
      println("received message:" +msg.toString)
       msg.destination.target match {
         case pl.setblack.lsa.events.System => processSysMessage(msg.event, connection)
         case x => println("nic nie kumam")
       }

      // backend.newMessage(msg)
    }
    connection.onclose = { (event: org.scalajs.dom.raw.Event) ⇒

    }
    connection
  }
  private def getWebsocketUri(document: Document, nameOfChatParticipant: String): String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"

    s"$wsProtocol://${dom.document.location.host}/board?name=$nameOfChatParticipant"
  }
}
