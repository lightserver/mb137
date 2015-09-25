package pl.setblack.lsa.io

import java.io.{ObjectInputStream, ObjectOutputStream}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import upickle.default._

class FileStore extends Storage{
  override def save(value: String, path: Seq[String]): Unit = {

    val output = Files.newBufferedWriter(Paths.get("fileStore", path:_*))
    output.write(value)
    output.close()
  }

  override def load(path: Seq[String]): Option[String] = {
    val input = Files.newBufferedReader(Paths.get("fileStore", path:_*))
    val line = input.readLine()
    input.close()
    Some(line)
  }
}



class SysStorage extends FileStore {

}