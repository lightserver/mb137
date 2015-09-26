package pl.setblack.mb137.data

import pl.setblack.lsa.events.Node
import pl.setblack.lsa.io.Storage
import upickle._


abstract class BoardSystem {
  val mainNode = createMainNode()
  val storage = createStorage()
  mainNode.registerDomain(Seq("default"), new BoardDomain("default"))

  load()

   def createMainNode():Node

   def createStorage() : Storage


  def enterMessage( txt: String) = {
    val newPost = NewPost(txt, "irek71")
    mainNode.sendEvent(BoardEvent.writeBoardEvent(newPost) ,Seq("default"))
  }

  def getBoardMutable():BoardMutable  = {
    mainNode.getDomainObject(Seq("default")).asInstanceOf[BoardMutable]
  }

  def save() = {
    this.mainNode.saveDomains(storage)
  }

  def load() = {
    this.mainNode.loadDomains(storage)
  }
}



