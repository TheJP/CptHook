package ch.fhnw.cpthook.json

import org.json4s._
import ch.fhnw.cpthook.model.Block

class TypeFormats extends Formats {
    val dateFormat = DefaultFormats.lossless.dateFormat
    override val typeHints = ShortTypeHints(List(classOf[Block]))
    override def typeHintFieldName = "type"
}