package pl.setblack.mb137.web

import org.scalajs.dom
import org.scalajs.dom.raw._
import pl.setblack.mb137.data.BoardMessage
import upickle._
/**
 * Created by jarek on 9/14/15.
 */
class ServerConnection(val backend : BoardBackend)  {
  val connection: WebSocket = startWs()


  def sendBoardMessage( msg: BoardMessage): Unit = {
    connection.send(write(msg))
  }

  private def  startWs() = {
    val connection = new WebSocket(getWebsocketUri(dom.document, "irek"))
    connection.onopen = { (event: Event) ⇒
      println("connection done");

      event
    }
    connection.onerror = { (event: ErrorEvent) ⇒

    }
    connection.onmessage = { (event: MessageEvent) ⇒
        val msg = read[BoardMessage] (event.data.toString)
       backend.newMessage(msg)
    }
    connection.onclose = { (event: Event) ⇒

    }
    connection
  }
  private def getWebsocketUri(document: Document, nameOfChatParticipant: String): String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"

    s"$wsProtocol://${dom.document.location.host}/board?name=$nameOfChatParticipant"
  }
}
