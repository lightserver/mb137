package pl.setblack.mb137.data

import pl.setblack.lsa.events.Node
import upickle._


abstract class BoardSystem {
  val mainNode = createMainNode()
  mainNode.registerDomain(Seq("default"), new BoardDomain("default"))


   def createMainNode():Node


  def enterMessage( txt: String) = {
    val newPost = NewPost(txt, "irek71")
    mainNode.sendEvent(BoardEvent.writeBoardEvent(newPost) ,Seq("default"))
  }
}



