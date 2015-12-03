package ch.fhnw.cpthook

import ch.fhnw.ether.controller.DefaultController
import ch.fhnw.ether.controller.event.IPointerEvent

/**
 * Custom controller, which disables navigation tool.
 * The rest of the code is mostly equivalent to the DefaultController.
 */
class CptHookController extends DefaultController {
  override def pointerPressed(event: IPointerEvent): Unit = {
    if(getUI != null && getUI.pointerPressed(event)){ return; }
    getCurrentTool.pointerPressed(event)
  }
  override def pointerReleased(event: IPointerEvent): Unit = {
    if(getUI != null && getUI.pointerReleased(event)){ return; }
    getCurrentTool.pointerReleased(event)
  }
  override def pointerMoved(event: IPointerEvent): Unit = {
    if(getUI != null){ getUI.pointerMoved(event) }
    getCurrentTool.pointerMoved(event)
  }
  override def pointerDragged(event: IPointerEvent): Unit = {
    if(getUI != null && getUI.pointerDragged(event)){ return; }
    getCurrentTool.pointerDragged(event)
  }
  override def pointerScrolled(event: IPointerEvent) = super.getCurrentTool.pointerScrolled(event)
}