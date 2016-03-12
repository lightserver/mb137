package pl.setblack.mb137.data

import java.util.Date

import pl.setblack.lsa.events.Node
import pl.setblack.lsa.io.Storage
import upickle._


abstract class BoardSystem {
  val  storage = createStorage()
  val mainNode = createMainNode()

  val boardRef = mainNode.registerDomain(Seq("default"), new BoardDomain("default", Seq("default")))

  load()
  resync()

   def createMainNode():Node

   def createStorage() : Storage


  def enterMessage( txt: String, author:String) = {
    val uuid = java.util.UUID.randomUUID.toString
    val newPost = NewPost(txt, author, new Date().getTime(), uuid)
    boardRef.send(newPost)
  }

  def deleteMessage( uuid: String) = {
   val deletePost = DeletePost( uuid)

    boardRef.send(deletePost)
  }



  def save() = {
    //this.mainNode.saveDomains(storage)
  }

  def load() = {
    this.mainNode.loadDomains()
  }

  def resync() = {

    this.mainNode.resync()
  }
}



