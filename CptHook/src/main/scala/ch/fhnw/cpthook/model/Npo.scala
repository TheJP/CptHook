package ch.fhnw.cpthook.model

import ch.fhnw.ether.scene.I3DObject

trait Npo {
  def npoType: NpoType[this.type]
}

trait NpoType[-T <: Npo] {
  def to3DObject(npo: T): I3DObject
}