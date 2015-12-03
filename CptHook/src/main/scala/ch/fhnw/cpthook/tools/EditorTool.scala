package ch.fhnw.cpthook.tools

import ch.fhnw.cpthook.model.Vec2
import ch.fhnw.ether.controller.IController
import ch.fhnw.ether.controller.event.IPointerEvent
import ch.fhnw.ether.controller.tool.AbstractTool
import ch.fhnw.ether.scene.camera.ICamera
import ch.fhnw.util.math.Vec3

/**
 * Tool, which is used in the editor.
 * Responsible for movement and level changes (e.g. block adding).
 */
class EditorTool(val controller: IController, val camera: ICamera) extends AbstractTool(controller) {

  val OffsetScale = 0.2f

  var startX = 0
  var cameraPosition = camera.getPosition
  var cameraTarget = camera.getTarget

  override def pointerPressed(event: IPointerEvent): Unit = {
    if(event.getButton == IPointerEvent.BUTTON_2){
      startX = event.getX
      cameraPosition = camera.getPosition
      cameraTarget = camera.getTarget
    }
  }

  override def pointerDragged(event: IPointerEvent): Unit = {
    if(event.getButton == IPointerEvent.BUTTON_2){
      val offset = new Vec3(event.getX - startX, 0, 0).scale(OffsetScale);
      camera.setTarget(cameraTarget.add(offset))
      camera.setPosition(cameraPosition.add(offset))
    }
  }
}