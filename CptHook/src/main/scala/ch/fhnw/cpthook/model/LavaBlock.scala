package ch.fhnw.cpthook.model

import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.FixtureDef

import ch.fhnw.cpthook.model.Vec2.toVec3
import ch.fhnw.ether.scene.I3DObject

/**
 * Block which hurts the player.
 */
case class Lava(var position: Position, var size: Size) extends Npo {
  def npoType = LavaType
}

object LavaType extends NpoType[Lava] {
  def to3DObject(npo: Lava): I3DObject = createDefaultCube(materialLava, npo.position toVec3 0f, npo.size)
  def toBox2D(npo: Lava): (BodyDef, FixtureDef) = createDefaultBox2D(npo.position, npo.size)
}
