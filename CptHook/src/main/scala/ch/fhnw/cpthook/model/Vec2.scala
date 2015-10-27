package ch.fhnw.cpthook.model

class Vec2[T](var pair: (T, T))(implicit num: Numeric[T]) {
  import num._
  def +(other: Vec2[T]): Vec2[T] = new Vec2(pair._1 + other.pair._1, pair._2 + other.pair._2)
  def -(other: Vec2[T]): Vec2[T] = new Vec2(pair._1 - other.pair._1, pair._2 - other.pair._2)
  def +=(other: Vec2[T]): Unit = pair = (pair._1 + other.pair._1, pair._2 + other.pair._2)
  def -=(other: Vec2[T]): Unit = pair = (pair._1 - other.pair._1, pair._2 - other.pair._2)
}