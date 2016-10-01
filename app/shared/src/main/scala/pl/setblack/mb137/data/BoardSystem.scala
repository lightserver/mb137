package pl.setblack.mb137.data

import java.util.Date

import pl.setblack.lsa.events.Node
import pl.setblack.lsa.io.Storage
import pl.setblack.lsa.security.SecurityProvider
import upickle._

import scala.concurrent.{ExecutionContext, Future}


class BoardSystem(val mainNode : Node) {

  val boardRef = mainNode.registerDomain(Seq("default"), new BoardDomain("default", Seq("default")))

  mainNode.loadDomains()

  mainNode.resync()


  def enterMessage( txt: String, author:String) = {
    val uuid = java.util.UUID.randomUUID.toString
    val newPost = NewPost(txt, author, new Date().getTime(), uuid)
    println(s" sending to boardref ${boardRef}")
    boardRef.send(newPost)
  }

  def deleteMessage( uuid: String) = {
   val deletePost = DeletePost( uuid)

  boardRef.send(deletePost)
  }






}



