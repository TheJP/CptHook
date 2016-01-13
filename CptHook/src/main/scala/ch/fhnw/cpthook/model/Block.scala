package ch.fhnw.cpthook.model

import ch.fhnw.ether.scene.mesh.IMesh
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.FixtureDef
import ch.fhnw.ether.scene.mesh.material.ColorMapMaterial
import ch.fhnw.ether.image.Frame
import ch.fhnw.ether.scene.mesh.material.IMaterial
import ch.fhnw.util.math.Vec3
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.Primitive
import ch.fhnw.ether.scene.mesh.DefaultMesh
import ch.fhnw.ether.scene.mesh.MeshUtilities
import ch.fhnw.ether.scene.mesh.IMesh.Queue
import ch.fhnw.util.math.Mat4
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.World
import org.jbox2d.dynamics.Body
import ch.fhnw.cpthook.tools.GameContactListener
import ch.fhnw.cpthook.tools.ContactUpdates
import org.jbox2d.dynamics.Fixture
import org.jbox2d.dynamics.contacts.Contact
import ch.fhnw.ether.scene.mesh.material.Texture
import ch.fhnw.cpthook.SoundManager

abstract class Block(var position: Position, var size: Size, var texture: Texture) extends Entity {
  def getFriction: Float
  def getRestitution: Float

  def linkBox2D(world: World): Unit = Block.createDefaultBox2D(world, this)
  def toMesh(): ch.fhnw.ether.scene.mesh.IMesh = Block.createDefaultCube(texture, position, size)
}

/**
 * Block, which allows access to a game state controller.
 */
abstract class GameStateBlock(position: Position, size: Size, texture: Texture) extends Block(position, size, texture) with IGameStateChanger {
  private var controller: IGameStateController = null;
  /** Returns the controller. Can be null! */
  def getController = controller
  def init(controller: IGameStateController): Unit = { this.controller = controller }
}

class GrassBlock(position: Position, size: Size) extends Block(position, size, Block.GrassTexture) {
  def getFriction = 0.2f
  def getRestitution = 0.1f
}

class DirtBlock(position: Position, size: Size) extends Block(position, size, Block.DirtTexture) {
  def getFriction = 0.2f
  def getRestitution = 0.1f
}

class IceBlock(position: Position, size: Size) extends Block(position, size, Block.IceTexture) {
  def getFriction = 0.2f
  def getRestitution = 0.1f
}

/**
 * Block, which kills cpthook if he touches it.
 */
class LavaBlock(position: Position, size: Size) extends GameStateBlock(position, size, Block.LavaTexture) with EntityActivatable with ContactUpdates {
  def getFriction = 0.2f
  def getRestitution = 0.1f
  //Store body, when available
  var body: Body = null
  override def linkBox2D(world: World): Unit = { body = Block.createDefaultBox2D(world, this) }
  def deactivate(): Unit = { body = null }
  def activate(gameContactListener: GameContactListener): Unit =
      if(body != null) { gameContactListener.register(this, body.getFixtureList) }
  //Kill when collision is detected
  def beginContact(self: Fixture, other: Fixture, contact: Contact): Unit = other.getBody.getUserData match {
    case player: Player => if(getController != null){ getController.gameOver }
    case monster: Monster => if(getController != null){ getController.killMonser(other.getBody) }
    case _ =>
  }
  def endContact(self: Fixture, other: Fixture, contact: Contact): Unit = {}
}

/**
 * Block that describes one (of potentially many) targets in the containing level.
 * If cpthook touches this block ingame the player wins.
 */
class TargetBlock(position: Position, size: Size) extends GameStateBlock(position, size, Block.TargetTexture) with EntityActivatable with ContactUpdates {
  def getFriction = 0.2f
  def getRestitution = 0.1f
  //Store body, when available
  var body: Body = null
  override def linkBox2D(world: World): Unit = { body = Block.createDefaultBox2D(world, this) }
  def activate(gameContactListener: GameContactListener): Unit =
    if(body != null){ gameContactListener.register(this, body.getFixtureList) }
  def deactivate(): Unit = { body = null }
  //Win game detection
  def beginContact(self: Fixture, other: Fixture, contact: Contact): Unit = other.getBody.getUserData match {
    case player: Player if !other.isSensor()=> if(getController != null){ getController.win }
    case _ =>
  }
  def endContact(self: Fixture, other: Fixture, contact: Contact): Unit = {}
}

/**
 * Trampoline block for better jumping action.
 */
class TrampolineBlock(position: Position, size: Size) extends Block(position, size, Block.TrampolineTexture) with EntityActivatable with ContactUpdates {
  def getFriction = 0.2f
  def getRestitution = 0.1f
  //Store body, when available
  var body: Body = null
  override def linkBox2D(world: World): Unit = { body = Block.createDefaultBox2D(world, this) }
  def deactivate(): Unit = { body = null }
  def activate(gameContactListener: GameContactListener): Unit =
    if(body != null){ gameContactListener.register(this, body.getFixtureList) }
  //Apply jump effect when collision is detected
  def beginContact(self: Fixture, other: Fixture, contact: Contact): Unit = {
    if (!other.isSensor()) {
      other.getBody.setLinearVelocity(new org.jbox2d.common.Vec2(0, 20))
      SoundManager.playSound(SoundManager.BumpSound, false, false)
    } 
  }
  def endContact(self: Fixture, other: Fixture, contact: Contact): Unit = {}
}

object Block {
  
  val GrassTexture = Entity.loadTexture("../assets/grass.png")
  val DirtTexture = Entity.loadTexture("../assets/dirt.png")
  val IceTexture = Entity.loadTexture("../assets/ice.png")
  val LavaTexture = Entity.loadTexture("../assets/lava.png")
  val TargetTexture = Entity.loadTexture("../assets/target.png")
  val TrampolineTexture = Entity.loadTexture("../assets/jump.png")

  /**
   * Texture coordinates for default cube
   */
  val textureCoordinates = Array(0f, .34f, 0f, .66f, 1f, .66f, 0f, .34f, 1f, .66f, 1f, .34f,      //back
                                 0f, .34f, 0f, .66f, 1f, .66f, 0f, .34f, 1f, .66f, 1f, .34f,      //front
                                 0, 0f, 0, .33f, 1f, .33f, 0, 0f, 1f, .33f, 1f, 0f,               //bottom
                                 0, .67f, 0, 1f, 1f, 1f, 0, 0.67f, 1f, 1f, 1f, 0.67f,             //top
                                 1, .66f, 1f, .34f, 0f, .34f, 1f, .66f, 0f, .34f, 0f, .66f,       //left
                                 0f, .34f, 0f, .66f, 1f, .66f, 0f, .34f, 1f, .66f, 1f, .34f )     //right
  
  /**
   * Creates a default cube with the given material.
   */
  def createDefaultCube(texture: Texture, position: Vec3, size: Vec3) : IMesh = {
    val geometry = DefaultGeometry.createVM(Primitive.TRIANGLES, MeshUtilities.UNIT_CUBE_TRIANGLES, textureCoordinates);
    val mesh = new DefaultMesh(new ColorMapMaterial(texture), geometry, Queue.DEPTH)
    mesh.setPosition(position add (size scale 0.5f))
    mesh.setTransform(Mat4 scale size)
    mesh
  }

  /**
   * Create a default Box2D model.
   * Square form and default values.
   */
  def createDefaultBox2D(world: World, block: Block): Body = {
    val bodyDef: BodyDef = new BodyDef
    bodyDef.position.set(block.position.x + block.size.width / 2f, block.position.y + block.size.height / 2f)
    bodyDef.`type` = BodyType.STATIC

    val shape: PolygonShape = new PolygonShape
    shape.setAsBox(block.size.width / 2f, block.size.height / 2f)

    val fixtureDef: FixtureDef = new FixtureDef
    fixtureDef.shape = shape
    fixtureDef.friction = block.getFriction;
    fixtureDef.restitution = block.getRestitution;

    val body: Body = world.createBody(bodyDef)
    body.createFixture(fixtureDef)
    body.setUserData(block)
    body
  }
  
}