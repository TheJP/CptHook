package ch.fhnw.cpthook.model

import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.World
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ch.fhnw.cpthook.InputManager
import ch.fhnw.cpthook.tools.GameContactListener
import ch.fhnw.ether.image.Frame
import ch.fhnw.ether.scene.mesh.IMesh
import ch.fhnw.ether.scene.mesh.material.Texture
import com.fasterxml.jackson.annotation.JsonIgnore

abstract class Entity() {
  def position: Position
  def toMesh: IMesh
  def linkBox2D(world: World): Unit
}

trait EntitiyUpdatable {
  def update(inputManager: InputManager, time: Double): Unit
}

trait EntityActivatable {
  def activate(gameContactListener: GameContactListener): Unit
  def deactivate(): Unit
}

/**
 * Trait that allows an entity to change the game state.
 * (This includes transitions like GameOver or Win.)
 */
trait IGameStateChanger {
  def init(controller: IGameStateController)
}

/**
 * Trait that defines functions to change the game state.
 */
trait IGameStateController {
  def gameOver
  def win
  def killMonser(body: Body)
  def switchGravity
  def setCheckpoint(point: CheckpointBlock)
}

object Entity {
  /**
   * loads a color map material
   */
  def loadTexture(path: String): Texture = {
    Frame.create(getClass.getResource(path)).getTexture()
  }
  
  val defaultTextureCoordinates = Array( 0f, 0f, 1f, 0f, 1f, 1f, 0f, 0f, 0f, 1f, 1f, 1f )
  def twoDimensionalPlane(width: Float, height: Float, depth: Float): Array[Float] = {
    val halfWidth = width / 2f
    val halfHeight = height / 2f
    Array(-halfWidth, -halfHeight, depth, halfWidth, -halfHeight, depth, halfWidth, halfHeight, depth,
        -halfWidth, -halfHeight, depth, -halfWidth, halfHeight, depth, halfWidth, halfHeight, depth)
  }
  
}