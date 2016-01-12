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

abstract class Block(var position: Position, var size: Size, var materialPath: String) extends Entity {
  def getFriction: Float
  def getRestitution: Float
  
  def toBox2D(): (org.jbox2d.dynamics.BodyDef, org.jbox2d.dynamics.FixtureDef) = Block.createDefaultBox2D(this)
  def toMesh(): ch.fhnw.ether.scene.mesh.IMesh = Block.createDefaultCube(materialPath, position, size)
}

class GrassBlock(position: Position, size: Size) extends Block(position, size, "../assets/grass.png") {
  def getFriction = 0.2f
  def getRestitution = 0.1f
}

class DirtBlock(position: Position, size: Size) extends Block(position, size, "../assets/dirt.png") {
  def getFriction = 0.2f
  def getRestitution = 0.1f
}

class IceBlock(position: Position, size: Size) extends Block(position, size, "../assets/ice.png") {
  def getFriction = 0.2f
  def getRestitution = 0.1f
}

class LavaBlock(position: Position, size: Size) extends Block(position, size, "../assets/lava.png") {
  def getFriction = 0.2f
  def getRestitution = 0.1f
}

object Block {
  
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
  def createDefaultCube(materialPath: String, position: Vec3, size: Vec3) : IMesh = {
    val g = DefaultGeometry.createVM(Primitive.TRIANGLES, MeshUtilities.UNIT_CUBE_TRIANGLES, textureCoordinates);
    val mesh = new DefaultMesh(Entity.loadMaterial(materialPath), g, Queue.DEPTH);
    mesh.setPosition(position add (size scale 0.5f))
    mesh.setTransform(Mat4 scale size)
    mesh
  }
  
  

  /**
   * Create a default Box2D model.
   * Square form and default values.
   */
  def createDefaultBox2D(block: Block): (BodyDef, FixtureDef) = {
    val bodyDef: BodyDef = new BodyDef
    bodyDef.position.set(block.position.x + block.size.width / 2f, block.position.y + block.size.height / 2f)
    bodyDef.`type` = BodyType.STATIC

    val shape: PolygonShape = new PolygonShape
    shape.setAsBox(block.size.width / 2f, block.size.height / 2f)

    val fixtureDef: FixtureDef = new FixtureDef
    fixtureDef.shape = shape
    fixtureDef.friction = block.getFriction;
    fixtureDef.restitution = block.getRestitution;

    (bodyDef, fixtureDef)
  }
  
}