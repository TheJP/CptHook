package ch.fhnw.cpthook.model

import ch.fhnw.ether.scene.I3DObject
import ch.fhnw.ether.scene.mesh.IMesh
import ch.fhnw.ether.scene.mesh.MeshUtilities
import ch.fhnw.util.math.Mat4
import ch.fhnw.util.math.Vec3
import ch.fhnw.ether.scene.mesh.material.ColorMaterial
import ch.fhnw.util.color.RGBA
import ch.fhnw.ether.scene.mesh.material.ShadedMaterial
import ch.fhnw.util.color.RGB

case class Block(var position: Position, var size: Size) extends Npo {
  def npoType = BlockType
}

object BlockType extends NpoType[Block]{
  val material = new ShadedMaterial(RGB.GREEN)
  def to3DObject(npo: Block): I3DObject = {
    val mesh = MeshUtilities.createCube(material)
    mesh.setPosition((npo.position toVec3 0) add (npo.size scale 0.5f))
    mesh.setTransform(Mat4.scale(npo.size))
    mesh
  }
}
