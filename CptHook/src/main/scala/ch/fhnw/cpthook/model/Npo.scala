package ch.fhnw.cpthook.model

import ch.fhnw.ether.scene.I3DObject
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.FixtureDef

abstract class Npo {
  def npoType: NpoType[this.type]
  def to3DObject: I3DObject = npoType.to3DObject(this)
  def toBox2D: (BodyDef, FixtureDef) = npoType.toBox2D(this)
}

trait NpoType[-T <: Npo] {
  def to3DObject(npo: T): I3DObject
  def toBox2D(npo: T): (BodyDef, FixtureDef)
}