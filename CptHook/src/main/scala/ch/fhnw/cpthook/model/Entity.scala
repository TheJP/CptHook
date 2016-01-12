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

abstract class Entity {
  def toMesh(): IMesh
  def toBox2D(): (BodyDef, FixtureDef)
}

trait EntitiyUpdatable {
  def update(inputManager: InputManager, time: Double): Unit
}

trait EntitySubscribable {
  def subscribe(): Unit
  def unsubscribe(): Unit
}

object Entity {
  /**
   * loads a color map material
   */
  def loadMaterial(path: String): ColorMapMaterial = {
    new ColorMapMaterial(Frame.create(getClass.getResource(path)).getTexture())
  }
  
}