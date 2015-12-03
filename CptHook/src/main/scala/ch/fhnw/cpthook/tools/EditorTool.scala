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

/**
 * Tool, which is used in the editor.
 * Responsible for movement and level changes (e.g. block adding).
 */
class EditorTool(val controller: IController, val camera: ICamera, val viewModel: ILevelViewModel)
  extends AbstractTool(controller) {

  val OffsetScale = 0.2f

  var startX = 0
  var cameraPosition = camera.getPosition
  var cameraTarget = camera.getTarget

  override def pointerPressed(event: IPointerEvent): Unit = event.getButton match {
    case IPointerEvent.BUTTON_2 =>
      startX = event.getX
      cameraPosition = camera.getPosition
      cameraTarget = camera.getTarget
    case IPointerEvent.BUTTON_1 =>
      val viewCameraState = getController.getRenderManager.getViewCameraState(event.getView)
      val isHit = (mesh: (Npo, I3DObject)) => PickUtilities.pickObject(PickMode.POINT, event.getX, event.getY, 0, 0, viewCameraState, mesh._2) < Float.PositiveInfinity
      viewModel.npos.filter { isHit } foreach { mesh => viewModel.removeNpo(mesh._1) }
  }

  /**
   * Moves the view horizontally in the editor mode.
   */
  override def pointerDragged(event: IPointerEvent): Unit = {
    if(event.getButton == IPointerEvent.BUTTON_2){
      val offset = new Vec3(event.getX - startX, 0, 0).scale(OffsetScale);
      camera.setTarget(cameraTarget.add(offset))
      camera.setPosition(cameraPosition.add(offset))
    }
  }

}