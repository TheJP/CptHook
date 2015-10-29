package ch.fhnw.cpthook

import ch.fhnw.cpthook.json.JsonSerializer
import ch.fhnw.cpthook.model._

object Main extends App {
  JsonSerializer.writeLevel("save.json", Level(Size(10, 10), Position(1, 6), List(Block(Position(3, 4), Size(2, 2)))))
  println(JsonSerializer.readLevel("save.json"))
}