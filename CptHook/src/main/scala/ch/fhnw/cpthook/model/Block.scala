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

abstract class Block extends Npo with IBlock {
  
}

object Block {
  
  /**
   * 
   */
  def createMaterial(path: String): ColorMapMaterial = {
    new ColorMapMaterial(Frame.create(getClass.getResource(path)).getTexture())
  }
  
  /**
   * Texture coordinates for default cuave
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
    val mesh = new DefaultMesh(createMaterial(materialPath), g, Queue.DEPTH);
    mesh.setPosition(position add (size scale 0.5f))
    mesh.setTransform(Mat4 scale size)
    mesh
  }
  
}

trait IBlock {
  def toMesh(): IMesh
  def toBox2D(): (BodyDef, FixtureDef)
}