package ch.fhnw.cpthook.json

import org.json4s._
import ch.fhnw.cpthook.model.Npo
import ch.fhnw.cpthook.model.Ice
import ch.fhnw.cpthook.model.Lava
import ch.fhnw.cpthook.model.GrassBlock

class TypeFormats extends Formats {
    val dateFormat = DefaultFormats.lossless.dateFormat
    override val typeHints = ShortTypeHints(List(classOf[GrassBlock], classOf[Ice], classOf[Lava]))
    override def typeHintFieldName = "type"
}