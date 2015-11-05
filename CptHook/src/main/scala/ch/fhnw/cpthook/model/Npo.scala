package ch.fhnw.cpthook.model

import ch.fhnw.ether.scene.I3DObject

abstract class Npo {
  def npoType: NpoType[this.type]
  def to3DObject: I3DObject = npoType.to3DObject(this)
}

trait NpoType[-T <: Npo] {
  def to3DObject(npo: T): I3DObject
}