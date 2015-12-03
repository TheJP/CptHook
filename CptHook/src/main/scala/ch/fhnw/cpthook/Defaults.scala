package ch.fhnw.cpthook

import ch.fhnw.util.color.RGB
import ch.fhnw.util.math.Vec3

object Defaults extends Configuration {
  override val windowTitle = "CptHook"
  override val windowPosition = (100, 100)
  override val windowSize = (800, 600)
  override val ambient = RGB.BLACK
  override val lightColor = RGB.WHITE
  override val lightDirection = new Vec3(-0.5, 0.5, 1)
}