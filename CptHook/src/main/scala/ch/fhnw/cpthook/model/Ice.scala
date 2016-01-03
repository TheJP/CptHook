package ch.fhnw.cpthook.model

import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.BodyDef
import ch.fhnw.ether.scene.I3DObject

/**
 * Block which lets the player slide.
 */
case class Ice(var position: Position, var size: Size) extends Npo {
  def npoType = IceType
}

object IceType extends NpoType[Ice] {
  val IceFriction = 0.1f
  def to3DObject(npo: Ice): I3DObject = createDefaultCube(materialIce, npo.position toVec3 0f, npo.size)
  def toBox2D(npo: Ice): (BodyDef, FixtureDef) = {
    val box2D = createDefaultBox2D(npo.position, npo.size)
    box2D._2.friction = IceFriction
    box2D
  }
}
