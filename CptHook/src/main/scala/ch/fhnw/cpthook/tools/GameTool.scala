package ch.fhnw.cpthook.tools

import ch.fhnw.cpthook.viewmodel.ILevelViewModel
import ch.fhnw.ether.controller.IController
import ch.fhnw.ether.controller.event.IPointerEvent
import ch.fhnw.ether.controller.tool.AbstractTool
import ch.fhnw.ether.controller.tool.PickUtilities
import ch.fhnw.ether.controller.tool.PickUtilities.PickMode
import ch.fhnw.ether.scene.camera.ICamera
import ch.fhnw.util.math.Vec3
import ch.fhnw.ether.scene.I3DObject
import ch.fhnw.cpthook.model.Npo
import ch.fhnw.ether.controller.event.IKeyEvent
import com.jogamp.newt.event.KeyEvent

/**
 * Tool, which handles the game logic.
 */
class GameTool(val controller: IController, val camera: ICamera, val viewModel: ILevelViewModel)
  extends AbstractTool(controller) {
 
    override def keyPressed(event: IKeyEvent): Unit = {
    if(event.getKeyCode == KeyEvent.VK_M) {
      println("switching to editor mode")
      controller.setCurrentTool(new EditorTool(controller, camera, viewModel))
    }
  }
  
}