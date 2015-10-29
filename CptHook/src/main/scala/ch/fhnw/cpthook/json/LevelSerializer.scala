package ch.fhnw.cpthook.json

import ch.fhnw.cpthook.model.Level

trait LevelSerializer {
  def readLevel(filename: String): Level
  def writeLevel(filename: String, level: Level): Unit
}