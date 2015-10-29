package ch.fhnw.cpthook

import ch.fhnw.util.math.Vec3
import ch.fhnw.util.color.RGB

trait Configuration {
  def windowTitle: String
  def windowPosition: (Int, Int)
  def windowSize: (Int, Int)
  //Main directional light
  def ambient: RGB
  def lightColor: RGB
  def lightDirection: Vec3
}