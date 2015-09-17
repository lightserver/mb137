package pl.setblack.mb137.data

import pl.setblack.lsa.events.Node

abstract class BoardSystem {
  val mainNode = createMainNode()
  mainNode.registerDomain(Seq("default"), new BoardDomain("default"))


   def createMainNode():Node
}



