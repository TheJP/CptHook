package ch.fhnw.cpthook.model

case class Position(position: (Int, Int)) extends Vec2(position) {
  def x = pair._1
  def y = pair._2
}
