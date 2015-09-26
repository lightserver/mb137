package pl.setblack.mb137.web

import japgolly.scalajs.react.vdom.prefix_<^._

import org.scalajs.dom.document
import pl.setblack.lsa.events.{Event, DomainListener}
import pl.setblack.mb137.data.{BoardMutable, BoardDomain, BoardMessage, BoardState}
import upickle._
import scalajs.concurrent.JSExecutionContext.Implicits.runNow
import org.scalajs.dom.ext.Ajax
import scala.scalajs.js.timers._
import japgolly.scalajs.react._

import scala.scalajs.js.annotation.JSExport


class BoardBackend($: BackendScope[Unit, BoardState]) extends DomainListener[BoardMutable] {
  val connection = new ServerConnection(this)


  override def onDomainChanged(domainObj: BoardMutable, ev: Event): Unit =  {
    println("domain has changed")
    $.modState(_.copy(messages = domainObj.messages))
  }

  def onChange(e: ReactEventI) = {
  //  textEntered = e.target.value
    $.modState(_.copy(inputText = e.target.value))
  }


  def handleSubmit(e: ReactEventI) = {
    e.preventDefault()

    $.modState(s => {
      //val newMessage = BoardMessage("ireeg", s.inputText)
      connection.system.enterMessage(s.inputText)
      connection.system.save()
      s.copy( messages = connection.system.getBoardMutable().messages)
    })
  }

  def handleLoad(e: ReactEventI) = {
    e.preventDefault()

    $.modState(s => {
      //val newMessage = BoardMessage("ireeg", s.inputText)
      connection.system.load()
      //s.copy( messages = s.messages :+ newMessage)
      println("loaded")
      s.copy( messages = connection.system.getBoardMutable().messages)
    })
  }



  def newMessage(b : BoardMessage): Unit = {
    $.modState(s => {
      BoardState(s.messages :+ b, "","")
    })
  }
}


object BoardControllers {

  def initBoard() = {

    val PostedMessage = ReactComponentB[BoardMessage]("PostedMessage")
      .render(message => <.li(message.author, message.txt))
      .build

    val TopicBoard = ReactComponentB[Seq[BoardMessage]]("TopicBoard")
      .render(messages => <.div("topic", <.ol(messages.map(m => PostedMessage(m)))))
      .build



    val toDisp = Seq(BoardMessage("cze", "...."),
      BoardMessage("irreeg", "aaa"))

    val BoardApp = ReactComponentB[Unit]("TodoApp")
      .initialState(BoardState(toDisp, "", ""))
      .backend(new BoardBackend(_))
      .render((_, S, B) =>
      <.div(
        <.h3("TODO"),
        TopicBoard(S.messages),
        <.form(^.onSubmit ==> B.handleSubmit,
          <.input(^.onChange ==> B.onChange, ^.value := S.inputText),
          <.button("Add #", S.messages.length + 1),
          <.button(^.onClick ==> B.handleLoad)("Load")
        )
      )
      ).buildU

    React.render(BoardApp(), document.getElementById("react"))

  }





}

