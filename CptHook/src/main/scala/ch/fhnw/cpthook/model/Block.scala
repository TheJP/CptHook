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
  def to3DObject(npo: Block): I3DObject = createDefaultCube(materialGrass, npo.position toVec3 0f, npo.size)
  def toBox2D(npo: Block): (BodyDef, FixtureDef) = createDefaultBox2D(npo.position, npo.size)
}
