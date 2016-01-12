package ch.fhnw.cpthook

import ch.fhnw.ether.controller.IController
import ch.fhnw.ether.ui.UI
import java.util.List
import ch.fhnw.ether.ui.IWidget
import java.util.Arrays

object EtherHacks {
  
  def removeWidgets(controller: IController): Unit = {
   
    val uiClass = classOf[UI]
    val widgetsField = uiClass.getDeclaredField("widgets")
    widgetsField.setAccessible(true)
    var widgets = widgetsField.get(controller.getUI).asInstanceOf[List[IWidget]]
    
    widgets.synchronized {
      widgets.clear()
    }
    
    controller.getUI.updateRequest()
  }
  
}