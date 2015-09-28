package pl.setblack.lsa.io

import org.scalajs.dom

import scala.scalajs.js._
import upickle.default._

class WebStorage extends Storage {
  override def save(value: String, path: Seq[String]): Unit = {

    dom.localStorage.setItem(path.toString(), value)
    println("stored in " + path.toString() + "=>" + value )
  }

  override def load(path: Seq[String]): Option[String] = {

    dom.localStorage.getItem(path.toString()) match {
      case null => println ("nonik")
          None
      case x => println("sonik"  + x)
          Some(x)
    }
  }
}

