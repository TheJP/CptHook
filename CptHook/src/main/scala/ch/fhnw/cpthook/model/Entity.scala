package ch.fhnw.cpthook.model

import ch.fhnw.ether.scene.I3DObject
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.FixtureDef
import ch.fhnw.ether.scene.mesh.material.ColorMapMaterial
import ch.fhnw.ether.image.Frame
import ch.fhnw.ether.scene.mesh.MeshUtilities
import ch.fhnw.ether.scene.mesh.material.IMaterial
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.Primitive
import ch.fhnw.ether.scene.mesh.IMesh.Queue
import ch.fhnw.ether.scene.mesh.DefaultMesh
import ch.fhnw.util.math.Vec3
import ch.fhnw.util.math.Mat4
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.dynamics.BodyType
import ch.fhnw.ether.scene.mesh.IMesh
import ch.fhnw.cpthook.InputManager
import org.jbox2d.dynamics.World
import ch.fhnw.cpthook.tools.GameContactListener
import ch.fhnw.ether.scene.mesh.material.Texture
import org.jbox2d.dynamics.Body

abstract class Entity(var position: Position) {
  def toMesh(): IMesh
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