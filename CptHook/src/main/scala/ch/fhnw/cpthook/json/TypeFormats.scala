package ch.fhnw.cpthook.json

import org.json4s._
import ch.fhnw.cpthook.model.Entity
import ch.fhnw.cpthook.model.GrassBlock
import ch.fhnw.cpthook.model.IceBlock
import ch.fhnw.cpthook.model.LavaBlock
import ch.fhnw.cpthook.model.DirtBlock

class TypeFormats extends Formats {
    val dateFormat = DefaultFormats.lossless.dateFormat
    override val typeHints = ShortTypeHints(List(classOf[GrassBlock],
                                                 classOf[DirtBlock],
                                                 classOf[IceBlock],
                                                 classOf[LavaBlock]))
    override def typeHintFieldName = "type"
}