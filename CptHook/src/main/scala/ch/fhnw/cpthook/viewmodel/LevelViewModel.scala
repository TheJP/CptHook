package ch.fhnw.cpthook.viewmodel

import scala.collection.breakOut

import ch.fhnw.cpthook.model.Level
import ch.fhnw.cpthook.model.Npo
import ch.fhnw.cpthook.model.Player
import ch.fhnw.cpthook.model.Position
import ch.fhnw.ether.scene.I3DObject
import ch.fhnw.ether.scene.IScene
import ch.fhnw.ether.scene.mesh.MeshUtilities
import ch.fhnw.ether.scene.mesh.material.ShadedMaterial
import ch.fhnw.util.color.RGB

/**
 * Converts Level objects to 3d objects, which can be added to a Ether-GL IScene.
 * It also defines actions, which can be executed from the view.
 */
trait ILevelViewModel {
  def get3DObjects: Iterable[I3DObject]
  def npos: Map[Npo, I3DObject]
  def removeNpo(npo: Npo): Unit
  def addNpo(npo: Npo): Unit
  def getPlayer: Player
}

class LevelViewModel(val level: Level, val scene: IScene) extends ILevelViewModel {
  
    // special player handling for now
    var player: Player = new Player(level.start)
    scene.add3DObject(player.mesh) 

    var meshes: Map[Npo, I3DObject] = level.npos.map(npo => (npo, npo.to3DObject))(breakOut)
   
    //Add all meshes to the scene
    scene.add3DObjects(get3DObjects.toList:_*)

    //Player indicator: map += ???
    def get3DObjects = meshes.values
    def npos = meshes
    
    def removeNpo(npo: Npo): Unit = {
      scene.remove3DObject(meshes(npo))
      meshes -= npo
      level.npos = level.npos filter { _ != npo } //TODO: Improve model => no linear search
    }
    
    def addNpo(npo: Npo): Unit = {
      meshes += (npo -> npo.to3DObject)
      level.npos ::= npo
      scene.add3DObject(meshes(npo))
    }
    
    def getPlayer = player

}
