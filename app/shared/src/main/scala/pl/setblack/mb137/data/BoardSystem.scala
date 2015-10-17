package pl.setblack.mb137.data

import pl.setblack.lsa.events.Node
import pl.setblack.lsa.io.Storage
import upickle._


abstract class BoardSystem {
  val  storage = createStorage()
  val mainNode = createMainNode()

  mainNode.registerDomain(Seq("default"), new BoardDomain("default", Seq("default")))

  load()
  resync()

   def createMainNode():Node

   def createStorage() : Storage


  def enterMessage( txt: String, author:String) = {
    val newPost = NewPost(txt, author)
    mainNode.sendEvent(BoardEvent.writeBoardEvent(newPost) ,Seq("default"))
  }

  def getBoardMutable():BoardMutable  = {
    mainNode.getDomainObject(Seq("default")).asInstanceOf[BoardMutable]
  }

  def save() = {
    //this.mainNode.saveDomains(storage)
  }

  def load() = {
    this.mainNode.loadDomains()
  }

  def resync() = {
    println("resync...")
    this.mainNode.resync()
  }
}



