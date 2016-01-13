package ch.fhnw.cpthook.viewmodel

import scala.collection.breakOut
import ch.fhnw.cpthook.model.Level
import ch.fhnw.cpthook.model.Entity
import ch.fhnw.cpthook.model.Player
import ch.fhnw.cpthook.model.Position
import ch.fhnw.ether.scene.I3DObject
import ch.fhnw.ether.scene.IScene
import ch.fhnw.ether.scene.mesh.MeshUtilities
import ch.fhnw.ether.scene.mesh.material.ShadedMaterial
import ch.fhnw.util.color.RGB
import ch.fhnw.cpthook.json.JsonSerializer
import ch.fhnw.cpthook.model.SkyBox
import ch.fhnw.ether.scene.mesh.IMesh
import ch.fhnw.cpthook.SoundManager
import ch.fhnw.cpthook.model.CheckpointBlock
import ch.fhnw.cpthook.model.CheckpointBlock
import ch.fhnw.cpthook.model.CheckpointBlock
import ch.fhnw.cpthook.model.CheckpointBlock

/**
 * Converts Level objects to 3d objects, which can be added to a Ether-GL IScene.
 * It also defines actions, which can be executed from the view.
 */
trait ILevelViewModel {
  def getMeshes: Iterable[IMesh]
  def entities: Map[Entity, IMesh]
  def removeNpo(entity: Entity)
  def addNpo(entity: Entity)
  def getPlayer: Player
  def getLevel: Level
  def saveLevel(filename: String)
  def openLevel(filename: String)
  def loadLevel(level: Level)
  def addSkyBox(skyBox: I3DObject)
  def removeSkyBox(skyBox : I3DObject)
  def getCheckpoint: CheckpointBlock
  def setCheckpoint(point: CheckpointBlock)
}

class LevelViewModel(initialLevel: Level, private val scene: IScene) extends ILevelViewModel {

  // special player handling for now
  private var player: Player = null
  private var level: Level = null
  private var meshes: Map[Entity, IMesh] = Map()
  private var checkpoint: CheckpointBlock = null

  loadLevel(initialLevel)

  //Player indicator: map += ???
  def getMeshes = meshes.values
  def getLevel = level
  def entities = meshes
  def getCheckpoint = checkpoint
  def setCheckpoint(point: CheckpointBlock) = {
    if(checkpoint != null && checkpoint != point){ checkpoint.disable }
    checkpoint = point
  }

  def removeNpo(entity: Entity): Unit = {
    scene.remove3DObject(meshes(entity))
    meshes -= entity
    level.entities = level.entities filter { _ != entity } //TODO: Improve model => no linear search
    //TODO: Find better place for this
    SoundManager.playSound(SoundManager.BlockRemoveSound)
  }

  def addNpo(entity: Entity): Unit = {
    meshes += (entity -> entity.toMesh)
    level.entities ::= entity
    scene.add3DObject(meshes(entity))
    //TODO: Find better place for this
    SoundManager.playSound(SoundManager.BlockPlaceSound)
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
    meshes = level.entities.map(entity => (entity, entity.toMesh))(breakOut)
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
