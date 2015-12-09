package ch.fhnw.cpthook

import ch.fhnw.ether.controller.event.IKeyEvent
import scala.collection.mutable.HashSet
import scala.collection.mutable.Set

class InputManager {
  
  val keyPressed: Set[Int] = new HashSet
  val keyWasPressed: Set[Int] = new HashSet
  
  def clearWasPressed(): Unit = keyWasPressed.clear
  
  def handleKeyPressedEvent(event: IKeyEvent): Unit = {
    val keyCode = event.getKeyCode
    if (!event.isAutoRepeat()) {
      keyPressed += keyCode
      keyWasPressed += keyCode
    }    
  }
  
  def handleKeyReleasedEvent(event: IKeyEvent): Unit =  {
    if (!event.isAutoRepeat()) { 
      keyPressed -= event.getKeyCode
    } 
  }
  
}