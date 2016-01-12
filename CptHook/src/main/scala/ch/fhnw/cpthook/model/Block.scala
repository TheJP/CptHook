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
import ch.fhnw.ether.scene.mesh.material.Texture

abstract class Block(var position: Position, var size: Size, var texture: Texture) extends Entity {
  def getFriction: Float
  def getRestitution: Float
  
  def linkBox2D(world: World): Unit = Block.createDefaultBox2D(world, this)
  def toMesh(): ch.fhnw.ether.scene.mesh.IMesh = Block.createDefaultCube(texture, position, size)
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

class LavaBlock(position: Position, size: Size) extends Block(position, size, Block.LavaTexture) {
  def getFriction = 0.2f
  def getRestitution = 0.1f
}

class TargetBlock(position: Position, size: Size) extends Block(position, size, Block.TargetTexture) {
  def getFriction = 0.2f
  def getRestitution = 0.1f
}

object Block {
  
  val GrassTexture = Entity.loadTexture("../assets/grass.png")
  val DirtTexture = Entity.loadTexture("../assets/dirt.png")
  val IceTexture = Entity.loadTexture("../assets/ice.png")
  val LavaTexture = Entity.loadTexture("../assets/lava.png")
  val TargetTexture = Entity.loadTexture("../assets/target.png")

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
    val g = DefaultGeometry.createVM(Primitive.TRIANGLES, MeshUtilities.UNIT_CUBE_TRIANGLES, textureCoordinates);
    val mesh = new DefaultMesh(new ColorMapMaterial(texture), g, Queue.DEPTH)
    mesh.setPosition(position add (size scale 0.5f))
    mesh.setTransform(Mat4 scale size)
    mesh
  }

  /**
   * Create a default Box2D model.
   * Square form and default values.
   */
  def createDefaultBox2D(world: World, block: Block): Unit = {
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
  }
  
}