package ch.fhnw.cpthook.json

import org.json4s._
import ch.fhnw.cpthook.model.Block
import ch.fhnw.cpthook.model.Npo
import ch.fhnw.cpthook.model.Ice
import ch.fhnw.cpthook.model.Lava

class TypeFormats extends Formats {
    val dateFormat = DefaultFormats.lossless.dateFormat
    override val typeHints = ShortTypeHints(List(classOf[Block], classOf[Ice], classOf[Lava]))
    override def typeHintFieldName = "type"
}