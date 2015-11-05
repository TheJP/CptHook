package ch.fhnw.cpthook.model

import ch.fhnw.util.math.Vec3

class Vec2[T](var pair: (T, T))(implicit num: Numeric[T]) {
  import num._
  def +(other: Vec2[T]): Vec2[T] = new Vec2(pair._1 + other.pair._1, pair._2 + other.pair._2)
  def -(other: Vec2[T]): Vec2[T] = new Vec2(pair._1 - other.pair._1, pair._2 - other.pair._2)
  def +=(other: Vec2[T]): Unit = pair = (pair._1 + other.pair._1, pair._2 + other.pair._2)
  def -=(other: Vec2[T]): Unit = pair = (pair._1 - other.pair._1, pair._2 - other.pair._2)
  def toVec3(z: Float) = new Vec3(pair._1.toFloat, pair._2.toFloat, z) //TODO: Why has z to be the 2nd param?
}

object Vec2 {
  implicit def toVec3[T](v: Vec2[T])(implicit num: Numeric[T]) = v toVec3 1
}