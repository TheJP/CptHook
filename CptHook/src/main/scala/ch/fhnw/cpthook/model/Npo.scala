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

abstract class Npo {
  def npoType: NpoType[this.type]
  def to3DObject: I3DObject = npoType.to3DObject(this)
  def toBox2D: (BodyDef, FixtureDef) = npoType.toBox2D(this)
}

abstract class NpoType[-T <: Npo] {
  def to3DObject(npo: T): I3DObject
  def toBox2D(npo: T): (BodyDef, FixtureDef)

  val materialGrass = new ColorMapMaterial(Frame.create(getClass.getResource("../assets/grass.png")).getTexture())
  val materialDirt = new ColorMapMaterial(Frame.create(getClass.getResource("../assets/dirt.png")).getTexture())
  val materialJump = new ColorMapMaterial(Frame.create(getClass.getResource("../assets/jump.png")).getTexture())
  val materialTarget = new ColorMapMaterial(Frame.create(getClass.getResource("../assets/target.png")).getTexture())
  val materialGreen = new ColorMapMaterial(Frame.create(getClass.getResource("../assets/generic_green_for_janis.png")).getTexture())
  val materialLava = new ColorMapMaterial(Frame.create(getClass.getResource("../assets/lava.png")).getTexture())
  val materialIce = new ColorMapMaterial(Frame.create(getClass.getResource("../assets/ice.png")).getTexture())

  val vertices = MeshUtilities.UNIT_CUBE_TRIANGLES
  val texCoords = Array( 0f, .34f, 0f, .66f, 1f, .66f, 0f, .34f, 1f, .66f, 1f, .34f,      //back
                         0f, .34f, 0f, .66f, 1f, .66f, 0f, .34f, 1f, .66f, 1f, .34f,      //front
                         0, 0f, 0, .33f, 1f, .33f, 0, 0f, 1f, .33f, 1f, 0f,               //bottom
                         0, .67f, 0, 1f, 1f, 1f, 0, 0.67f, 1f, 1f, 1f, 0.67f,             //top
                         1, .66f, 1f, .34f, 0f, .34f, 1f, .66f, 0f, .34f, 0f, .66f,       //left
                         0f, .34f, 0f, .66f, 1f, .66f, 0f, .34f, 1f, .66f, 1f, .34f )     //right

  val DefaultFriction = 1f
  val DefaultRestitution = 0.5f

  /**
   * Creates a default cube with the given material.
   */
  def createDefaultCube(material: IMaterial, position: Vec3, size: Vec3) : I3DObject = {
    val g = DefaultGeometry.createVM(Primitive.TRIANGLES, vertices, texCoords);
    val mesh = new DefaultMesh(material, g, Queue.DEPTH);
    mesh.setPosition(position add (size scale 0.5f))
    mesh.setTransform(Mat4 scale size)
    mesh
  }

  /**
   * Create a default Box2D model.
   * Square form and default values.
   */
  def createDefaultBox2D(position: Position, size: Size): (BodyDef, FixtureDef) = {
    val bodyDef: BodyDef = new BodyDef
    bodyDef.position.set(position.x + size.width / 2f, position.y + size.height / 2f)
    bodyDef.`type` = BodyType.STATIC

    val shape: PolygonShape = new PolygonShape
    shape.setAsBox(size.width / 2f, size.height / 2f)

    val fixtureDef: FixtureDef = new FixtureDef
    fixtureDef.shape = shape
    fixtureDef.friction = DefaultFriction;
    fixtureDef.restitution = DefaultRestitution;

    (bodyDef, fixtureDef)
  }
}