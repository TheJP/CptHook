package ch.fhnw.cpthook.json

import ch.fhnw.cpthook.model._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, writePretty}
import java.io.FileInputStream
import java.io.FileOutputStream
import net.java.truecommons.io.Loan.loan
import java.io.PrintWriter

object JsonSerializer extends LevelSerializer {

  implicit val formats = new TypeFormats

  def readLevel(filename: String): Level = {
    loan(new FileInputStream(filename)) to {
      stream => read[Level](StreamInput(stream))
    }
  }

  def writeLevel(filename: String, level: Level): Unit = {
    loan(new PrintWriter(filename, "UTF-8")) to {
      writer => writePretty[Level, PrintWriter](level, writer)
    }
  }
}