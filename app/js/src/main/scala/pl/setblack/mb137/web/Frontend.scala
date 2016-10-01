package pl.setblack.mb137.web


import pl.setblack.lsa.browser.JSNexus
import pl.setblack.mb137.data.BoardSystem
import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}

import scala.scalajs.js


object Frontend extends js.JSApp {

  def main(): Unit = {
   import scala.concurrent.ExecutionContext.Implicits.global

    LoggerConfig.factory = PrintLoggerFactory()
    LoggerConfig.level = LogLevel.DEBUG


    val mainNode = new JSNexus().start()
    val sys   = new BoardSystem(mainNode)
    BoardControllers.initBoard(sys)
  }


}