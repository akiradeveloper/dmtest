package dmtest.stack

import java.nio.file.{Files, Paths, Path}

import dmtest._

import scala.sys.process._

object Luks {
  def format(s: Stack): Unit = {
    val keyFile = TempFile.text("aaaa")
    Shell(s"cryptsetup luksFormat ${s.bdev.path} --key-file=${keyFile}")
  }
}
case class Luks(backing: Stack) extends Stack {
  val keyFile = TempFile.text("aaaa")

  val name = RandName.alloc
  Shell(s"cryptsetup luksOpen ${backing.bdev.path} ${name} --key-file=${keyFile}")

  override protected def path: Path = Paths.get(s"/dev/mapper/${name}")

  override protected def terminate(): Unit = {
    Shell(s"cryptsetup luksClose ${name}")
    Files.deleteIfExists(keyFile)
  }
}
