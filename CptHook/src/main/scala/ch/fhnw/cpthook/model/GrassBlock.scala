package ch.fhnw.cpthook.model

import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.FixtureDef

import ch.fhnw.cpthook.model.Vec2.toVec3
import ch.fhnw.ether.scene.I3DObject

case class GrassBlock(var position: Position, var size: Size) extends Npo {
  def npoType = GrassBlockType
}

object GrassBlockType extends NpoType[GrassBlock]{
  def to3DObject(npo: GrassBlock): I3DObject = createDefaultCube(materialGrass, npo.position toVec3 0f, npo.size)
  def toBox2D(npo: GrassBlock): (BodyDef, FixtureDef) = createDefaultBox2D(npo.position, npo.size)
}
