package ch.fhnw.cpthook.json

import org.json4s._
import ch.fhnw.cpthook.model.Entity
import ch.fhnw.cpthook.model.GrassBlock
import ch.fhnw.cpthook.model.IceBlock
import ch.fhnw.cpthook.model.LavaBlock
import ch.fhnw.cpthook.model.DirtBlock
import ch.fhnw.cpthook.model.TrampolineBlock
import ch.fhnw.cpthook.model.TargetBlock
import ch.fhnw.cpthook.model.Monster
import ch.fhnw.cpthook.model.GravityBlock

class TypeFormats extends Formats {
    val dateFormat = DefaultFormats.lossless.dateFormat
    override val typeHints = ShortTypeHints(List(classOf[GrassBlock],
                                                 classOf[DirtBlock],
                                                 classOf[IceBlock],
                                                 classOf[LavaBlock],
                                                 classOf[TargetBlock],
                                                 classOf[TrampolineBlock],
                                                 classOf[Monster],
                                                 classOf[GravityBlock]))
    override def typeHintFieldName = "type"
}