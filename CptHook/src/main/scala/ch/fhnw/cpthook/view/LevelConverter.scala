package ch.fhnw.cpthook.view

import ch.fhnw.ether.scene.I3DObject
import ch.fhnw.cpthook.model.Level

/**
 * Converts Level objects to 3d objects, which can be added to a Ether-GL IScene.
 */
object LevelConverter {
  def convert(level: Level): List[I3DObject] = {
    level.npos.map { npo => npo.npoType.to3DObject(npo) }
  }
}
