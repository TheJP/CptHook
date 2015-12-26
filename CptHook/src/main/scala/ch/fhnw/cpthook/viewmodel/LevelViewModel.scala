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
import ch.fhnw.cpthook.json.JsonSerializer
import ch.fhnw.cpthook.model.SkyBox

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
  def saveLevel(filename: String): Unit
  def openLevel(filename: String): Unit
  def loadLevel(level: Level): Unit
  def addSkyBox(skyBox: I3DObject) : Unit
  def removeSkyBox(skyBox : I3DObject) : Unit
}

class LevelViewModel(initialLevel: Level, private val scene: IScene) extends ILevelViewModel {

  // special player handling for now
  private var player: Player = null
  private var level: Level = null
  private var meshes: Map[Npo, I3DObject] = Map()

  loadLevel(initialLevel)

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

  def saveLevel(filename: String) = JsonSerializer.writeLevel(filename, level)

  def openLevel(filename: String) = loadLevel(JsonSerializer.readLevel(filename))

  def loadLevel(level: Level): Unit = {
    //Unload old objects
    if(this.level != null) {
      scene.remove3DObjects(meshes.values.toList:_*)
      scene.remove3DObject(player.mesh)
    }
    //Load new ones
    this.level = level
    player = new Player(level.start)
    meshes = level.npos.map(npo => (npo, npo.to3DObject))(breakOut)
    scene.add3DObjects(meshes.values.toList:_*)
    scene.add3DObject(player.mesh)
  }
  
  def addSkyBox(skyBox: I3DObject) = {
    scene.add3DObject(skyBox)    
  }
  
  def removeSkyBox(skyBox : I3DObject) = {
    scene.remove3DObject(skyBox)
  }
}
