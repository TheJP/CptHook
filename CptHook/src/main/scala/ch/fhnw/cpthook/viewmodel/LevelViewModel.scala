package ch.fhnw.cpthook.viewmodel

import ch.fhnw.ether.scene.I3DObject
import ch.fhnw.cpthook.model.Level
import ch.fhnw.cpthook.model.Npo
import scala.collection.breakOut;

/**
 * Converts Level objects to 3d objects, which can be added to a Ether-GL IScene.
 * It also defines actions, which can be executed from the view.
 */
trait ILevelViewModel {
  def get3DObjects: Iterable[I3DObject]
}

class LevelViewModel(val level: Level) extends ILevelViewModel {
    var meshes: Map[Npo, I3DObject] = level.npos.map(npo => (npo, npo.to3DObject))(breakOut)
    //Player indicator: map += ???
    def get3DObjects = meshes.values
}
