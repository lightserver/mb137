package pl.setblack.lsa.io

import java.io.{ObjectInputStream, ObjectOutputStream}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import upickle.default._

class FileStore extends Storage {
  override def save(value: String, path: Seq[String]): Unit = {
    val fsPath = createPath(path)
    if (Files.exists(fsPath)) {
      Files.createDirectories(fsPath)
    }
    val output = Files.newBufferedWriter(fsPath)
    output.write(value)
    output.close()

  }

  override def load(path: Seq[String]): Option[String] = {
    val fsPath = createPath(path)
    if (Files.exists(fsPath)) {
      val input = Files.newBufferedReader(fsPath)
      val line = input.readLine()
      input.close()
      Some(line)
    } else {
      None
    }

  }

  def createPath(path: Seq[String]) = {
    Paths.get("fileStore", path: _*)
  }
}


class SysStorage extends FileStore {

}