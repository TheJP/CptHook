package ch.fhnw.cpthook.model

import ch.fhnw.ether.scene.I3DObject
import ch.fhnw.ether.scene.mesh.IMesh
import ch.fhnw.ether.scene.mesh.MeshUtilities
import ch.fhnw.util.math.Mat4
import ch.fhnw.util.math.Vec3
import ch.fhnw.ether.scene.mesh.material.ColorMapMaterial
import ch.fhnw.util.color.RGBA
import ch.fhnw.ether.scene.mesh.material.ShadedMaterial
import ch.fhnw.util.color.RGB
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.collision.shapes.PolygonShape
import ch.fhnw.ether.image.Frame
import ch.fhnw.ether.scene.mesh.DefaultMesh
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.Primitive
import ch.fhnw.ether.scene.mesh.IMesh.Queue;

case class Block(var position: Position, var size: Size) extends Npo {
  def npoType = BlockType
}

object BlockType extends NpoType[Block]{
  
//  val material = new ShadedMaterial(RGB.GREEN)
  val materialGrass = new ColorMapMaterial(Frame.create(getClass.getResource("..\\assets\\grass.png")).getTexture())
  val materialDirt = new ColorMapMaterial(Frame.create(getClass.getResource("..\\assets\\dirt.png")).getTexture())
  val materialJump = new ColorMapMaterial(Frame.create(getClass.getResource("..\\assets\\jump.png")).getTexture())
  val materialTarget = new ColorMapMaterial(Frame.create(getClass.getResource("..\\assets\\target.png")).getTexture()) 
  val materialGreen = new ColorMapMaterial(Frame.create(getClass.getResource("..\\assets\\generic_green_for_janis.png")).getTexture())
  
  val vertices = MeshUtilities.UNIT_CUBE_TRIANGLES
  val texCoords = Array( 0f, .34f, 0f, .66f, 1f, .66f, 0f, .34f, 1f, .66f, 1f, .34f,      //back
                         0f, .34f, 0f, .66f, 1f, .66f, 0f, .34f, 1f, .66f, 1f, .34f,      //front
                         0, 0f, 0, .33f, 1f, .33f, 0, 0f, 1f, .33f, 1f, 0f,               //bottom
                         0, .67f, 0, 1f, 1f, 1f, 0, 0.67f, 1f, 1f, 1f, 0.67f,             //top
                         1, .66f, 1f, .34f, 0f, .34f, 1f, .66f, 0f, .34f, 0f, .66f,       //left
                         0f, .34f, 0f, .66f, 1f, .66f, 0f, .34f, 1f, .66f, 1f, .34f )     //right

  def to3DObject(npo: Block): I3DObject = {
    val g = DefaultGeometry.createVM(Primitive.TRIANGLES, vertices, texCoords);
    val mesh = new DefaultMesh(materialGrass, g, Queue.DEPTH);
    mesh.setPosition((npo.position toVec3 0) add (npo.size scale 0.5f))
    mesh.setTransform(Mat4.scale(npo.size))
    mesh
  }
  
  def toBox2D(npo: Block): (BodyDef, FixtureDef) = {
    val bodyDef: BodyDef = new BodyDef
    bodyDef.position.set(npo.position.x + npo.size.width / 2f, npo.position.y + npo.size.height / 2f)
    bodyDef.`type` = BodyType.STATIC
    
    val shape: PolygonShape = new PolygonShape
    shape.setAsBox(npo.size.width / 2f, npo.size.height / 2f)
    
    val fixtureDef: FixtureDef = new FixtureDef
    fixtureDef.shape = shape
    fixtureDef.friction = 0.1f;        
    fixtureDef.restitution = 0.5f;
    
    (bodyDef, fixtureDef)
  }
}
