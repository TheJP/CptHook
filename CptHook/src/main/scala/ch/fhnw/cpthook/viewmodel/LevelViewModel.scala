package ch.fhnw.cpthook.viewmodel

import ch.fhnw.ether.scene.I3DObject
import ch.fhnw.cpthook.model.Level
import ch.fhnw.ether.scene.mesh.MeshLibrary

/**
 * Converts Level objects to 3d objects, which can be added to a Ether-GL IScene.
 * It also defines actions, which can be executed from the view.
 */
trait ILevelViewModel {
  def get3DObjects: Iterable[I3DObject]
}

class LevelViewModel(val level: Level) extends ILevelViewModel {
    var meshes = level.npos.map { npo => (npo, npo.npoType.to3DObject(npo)) } toMap
    //Player indicator: map += ???
    def get3DObjects = meshes.values
}
