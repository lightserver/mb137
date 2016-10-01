package pl.setblack.mb137.web

import japgolly.scalajs.react.CompScope.AnyDuringCallback
import japgolly.scalajs.react.vdom.prefix_<^._

import org.scalajs.dom.document
import pl.setblack.lsa.events.{Event, DomainListener}
import pl.setblack.mb137.data._
import pl.setblack.moment.Moment
import upickle._
import scalajs.concurrent.JSExecutionContext.Implicits.runNow
import org.scalajs.dom.ext.Ajax
import scala.scalajs.js.timers._
import japgolly.scalajs.react._
import scala.scalajs.js.annotation.JSExport


class BoardBackend(backendInitializer: BackendInitializer,
                   bs: BoardSystem,
                   $: BackendScope[Unit, BoardState]) extends DomainListener[BoardMutable, BoardEvent] {
  backendInitializer.backend = Some(this)
  var lastDomainState : Option[BoardMutable] = None

   bs.mainNode.registerDomainListener(this, Seq("default"))

  override def onDomainChanged(domainObj: BoardMutable, ev: Option[BoardEvent]): Unit = {
    println("DOMAN CHANGED")
    lastDomainState = Some(domainObj)
    $.modState(_.copy(messages = domainObj.getDisplayedMessages())).runNow()
  }

  def onChangeInputText(e: ReactKeyboardEventI)  : Callback= {
    e.persist()
    $.modState(_.copy(inputText = e.target.value))
  }

  def onChangeAuthor(e: ReactEventI) : Callback= {
    e.persist()
    $.modState(_.copy(author = e.target.value))
  }


  def handleSubmit(e: ReactEventI) = {
    e.preventDefault()
    println("sending....")
    $.modState(s => {
      bs.enterMessage(s.inputText, s.author)
      s
    })
  }

  def handleLoad(e: ReactEventI) = {
    e.preventDefault()

  }

  def init(): Unit = {
    $.modState(s => {
      s.copy(messages = Seq())
    }).runNow()
  }

  def newMessage(b: BoardMessage): Unit = {
    $.modState(s => {
      s.copy(messages = s.messages :+ b, inputText = "")
    })
  }
}

class MessageBackend(bs: BoardSystem, $: BackendScope[BoardMessage, Unit]) {
  def delete(uuid: String) =
    (x:Any) => Callback {

      bs.deleteMessage(uuid)
    }
}


class BackendInitializer extends DomainListener[BoardMutable, BoardEvent] {
  var backend: Option[BoardBackend] = None

  def init() = {
    backend.foreach(b => b.init())
  }

  override def onDomainChanged(domain: BoardMutable, ev: Option[BoardEvent]): Unit
  = backend.foreach(_.onDomainChanged(domain, ev))
}

object BoardControllers {


  def initBoard(sys: BoardSystem) = {
    val backendInitializer = new BackendInitializer

    val PostedMessage = ReactComponentB[BoardMessage]("PostedMessage")
      .stateless
      .backend(new MessageBackend(sys, _))
      .render(duringCallback =>
        <.li(
          <.span(^.className := "author")(duringCallback.props.author),
          <.span(^.className := "time")(Moment(duringCallback.props.timestamp).fromNow),
          <.span(duringCallback.props.txt),
          <.button(^.onClick ==> duringCallback.backend.delete(duringCallback.props.uuid))("x")
        )
      )
      .build

    val TopicBoard = ReactComponentB[Seq[BoardMessage]]("TopicBoard")
      .render(duringCallback => <.div("topic", <.ul(^.className := "messages")(duringCallback.props.map(m => PostedMessage(m)))))
      .build


    val toDisp = Seq()

    val BoardApp = ReactComponentB[Unit]("TodoApp")
      .initialState(BoardState(toDisp, "", "", "anonymous"))
      .backend(new BoardBackend(backendInitializer, sys, _))
      .render(duringCallback => {
        val S = duringCallback.state
        val B = duringCallback.backend
        <.div(
          <.h3("Galaxy News"),
          TopicBoard(S.messages),
          <.form(^.onSubmit ==> B.handleSubmit,
            <.label("nick"),
            <.input(^.onChange ==> B.onChangeAuthor, ^.value := S.author),
            <.textarea(^.onChange ==> B.onChangeInputText, ^.value := S.inputText),
            <.button("Send", S.messages.length + 1)
          )
        )
      }).buildU

    ReactDOM.render(BoardApp(), document.getElementById("react"))

  }


}

