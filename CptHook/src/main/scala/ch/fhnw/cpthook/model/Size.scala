package ch.fhnw.cpthook.model

case class Size(size: (Int, Int)) extends Vec2(size) {
  def width = pair._1
  def height = pair._2
}
