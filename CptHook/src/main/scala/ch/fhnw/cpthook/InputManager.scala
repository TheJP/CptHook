package ch.fhnw.cpthook

import ch.fhnw.ether.controller.event.IKeyEvent
import scala.collection.mutable.HashSet
import scala.collection.mutable.Set

class InputManager {
  
  val keyPressed: Set[Int] = new HashSet
  val keyWasPressed: Set[Int] = new HashSet
  
  def clearWasPressed(): Unit = keyWasPressed.clear
  
  def handleKeyPressedEvent(event: IKeyEvent): Unit = {
    if (!event.isAutoRepeat()) {
      keyPressed += event.getKeyCode
    }    
  }
  
  def handleKeyReleasedEvent(event: IKeyEvent): Unit =  {
    if (!event.isAutoRepeat()) {
      val keyCode = event.getKeyCode
      keyPressed -= keyCode
      keyWasPressed += keyCode
    } 
  }
  
}