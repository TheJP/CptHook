package ch.fhnw.cpthook.tools

import com.jogamp.newt.event.KeyEvent
import ch.fhnw.cpthook.Defaults
import ch.fhnw.cpthook.model.Block
import ch.fhnw.cpthook.model.Position
import ch.fhnw.cpthook.model.Size
import ch.fhnw.cpthook.model.Vec2.toVec3
import ch.fhnw.cpthook.viewmodel.ILevelViewModel
import ch.fhnw.ether.controller.IController
import ch.fhnw.ether.controller.event.IKeyEvent
import ch.fhnw.ether.controller.event.IPointerEvent
import ch.fhnw.ether.controller.tool.AbstractTool
import ch.fhnw.ether.controller.tool.PickUtilities
import ch.fhnw.ether.controller.tool.PickUtilities.PickMode
import ch.fhnw.ether.scene.I3DObject
import ch.fhnw.ether.scene.camera.ICamera
import ch.fhnw.ether.view.ProjectionUtilities
import ch.fhnw.util.math.Vec3
import ch.fhnw.ether.scene.camera.IViewCameraState
import com.sun.xml.internal.bind.annotation.OverrideAnnotationOf
import ch.fhnw.cpthook.model.Npo

/**
 * Tool, which is used in the editor.
 * Responsible for movement and level changes (e.g. block adding).
 */
class EditorTool(val controller: IController, val camera: ICamera, val viewModel: ILevelViewModel)
  extends AbstractTool(controller) {

  val OffsetScale = 0.2f
  var offsetX: Float = 0;
  var startX: Float = 0
  var offsetY: Float = 20

  object EditingState extends Enumeration { val Adding, Removing = Value }
  var editingState = EditingState.Adding
  
  camera.setUp(Defaults.cameraUp)
  camera.setTarget(new Vec3(offsetX, 0, 1))
  camera.setPosition(new Vec3(offsetX, 0, offsetY))

  /**
   * Remove nearest clicked block. Returns true if a block was removed and false otherwise.
   */
  private def remove(event: IPointerEvent)(implicit viewCameraState: IViewCameraState): Boolean = {
    val npo = findNearest(event)
    if(npo != null){ viewModel.removeNpo(npo) }
    return npo != null
  }

  /**
   * Finds nearest clicked block. If no block was clicked null is returned.
   */
  private def findNearest(event: IPointerEvent)(implicit viewCameraState: IViewCameraState): Npo = {
    val hitDistance = (mesh: I3DObject) => PickUtilities.pickObject(PickMode.POINT, event.getX, event.getY, 0, 0, viewCameraState, mesh)
    val hits = viewModel.npos
      .map(npo => (hitDistance(npo._2) -> npo._1))
      .filter(_._1 < Float.PositiveInfinity)
    if(!hits.isEmpty) { return hits.minBy(_._1)._2 }
    return null
  }

  /**
   * Adds a block at the pointer position if possible.
   */
  private def add(event: IPointerEvent)(implicit viewCameraState: IViewCameraState): Unit = {
    val ray = ProjectionUtilities.getRay(viewCameraState, event.getX, event.getY)
    // check if we can hit the xy plane
    if(ray.getDirection.z != 0f) {
      val s: Float = -ray.getOrigin.z / ray.getDirection.z
      val p: Vec3 = ray.getOrigin.add(ray.getDirection.scale(s))
      val size = new Size(1, 1)
      viewModel.addNpo(new Block(new Position((p.x - size.x / 2).round.toInt, (p.y - size.y / 2).round.toInt), size))
    }
  }
  
  override def pointerPressed(event: IPointerEvent): Unit = event.getButton match {
    case IPointerEvent.BUTTON_2 | IPointerEvent.BUTTON_3=>
      startX = event.getX
    case IPointerEvent.BUTTON_1 =>
      implicit val viewCameraState = getController.getRenderManager.getViewCameraState(event.getView)
      val removed = remove(event)
      if(!removed){ add(event) }
      editingState = if(removed) EditingState.Removing else EditingState.Adding
    case default =>
      //TODO: Remove debug log
      println(s"the mouse key $default is not supported yet")
  }

  /**
   * Moves the view horizontally in the editor mode.
   */
  override def pointerDragged(event: IPointerEvent): Unit = event.getButton match {
    case IPointerEvent.BUTTON_2 | IPointerEvent.BUTTON_3 =>
      val delta = event.getX - startX
      offsetX += delta * OffsetScale
      camera.setTarget(new Vec3(offsetX, 0, 1))
      camera.setPosition(new Vec3(offsetX, 0, offsetY))
      startX = event.getX
    case IPointerEvent.BUTTON_1 =>
      implicit val viewCameraState = getController.getRenderManager.getViewCameraState(event.getView)
      if(editingState == EditingState.Adding){
        if(findNearest(event) == null){ add(event) }
      } else {
        remove(event)
      }
    case default =>
      //TODO: Remove debug log
      println(s"the mouse key $default is not supported yet")
  }
  
  override def keyPressed(event: IKeyEvent): Unit = {
    if(event.getKeyCode == KeyEvent.VK_M) {
      println("switching to game mode")
      controller.setCurrentTool(new GameTool(controller, camera, viewModel))
    }
  }
  
  override def pointerScrolled(event: IPointerEvent): Unit = {
    offsetY += event.getScrollY * OffsetScale
    camera.setPosition(new Vec3(offsetX, 0, offsetY))
  }

}