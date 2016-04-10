package pl.setblack.mb137.web

import org.scalajs.dom
import org.scalajs.dom.raw.{WebSocket, Document}
import pl.setblack.lsa.events._
import pl.setblack.lsa.io.UriProvider
import upickle.default._

class ServerConnection(val backend : BackendInitializer) extends UriProvider {
  val connectionData = new ConnectionData

  var system:ClientBoardSystem = null


  var storedId: Option[Long] = None

  val connection: WebSocket = startWs()

  private def processSysMessage(ev : Event, connection : WebSocket): Unit = {

    val ctrlEvent = ControlEvent.parseControlEvent(ev.content)
    ctrlEvent match {
      case RegisteredClient(id,serverId, token) => {
        system = new ClientBoardSystem(id, this, serverId)
        backend.init()
        system.mainNode.registerDomainListener(backend, Seq("default"))
      }

      case x =>
          println(s"have to ignore ${ev.content}")
        //system.mainNode.processSysMessage(ev, connectionData)

    }
  }

  private def startWs() = {
    val connection = new WebSocket(getWSUri())
    connection.onopen = { (event: org.scalajs.dom.raw.Event) ⇒

    }
    connection.onerror = { (event: org.scalajs.dom.raw.ErrorEvent) ⇒
      println(s"there was an error ${event.toString}")

    }
    connection.onmessage = { (event: org.scalajs.dom.raw.MessageEvent) ⇒
      val msg = read[NodeMessageTransport](event.data.toString).toNodeMessage

      msg.destination.target match {
        case pl.setblack.lsa.events.System => processSysMessage(msg.event, connection)
        case x => println("unknown message")
      }
      // backend.newMessage(msg)
    }
    connection.onclose = { (event: org.scalajs.dom.raw.Event) ⇒
    }

    connection
  }

  private def getWebsocketUri(document: Document, existingId: Option[String]): String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"
    val idString = existingId.getOrElse("begForId")


    s"$wsProtocol://${dom.document.location.host}/services/board?id=${idString}"
  }


  override def getWSUri(): String = {
    println(s" wsuri generating : ${storedId} ")
    getWebsocketUri(dom.document, storedId.map( _.toString) )
  }


}
