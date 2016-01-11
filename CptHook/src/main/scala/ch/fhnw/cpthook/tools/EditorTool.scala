package ch.fhnw.cpthook.tools

import com.jogamp.newt.event.KeyEvent
import ch.fhnw.cpthook.Defaults
import ch.fhnw.cpthook.ICptHookController
import ch.fhnw.cpthook.model.Block
import ch.fhnw.cpthook.model.Block
import ch.fhnw.cpthook.model.Ice
import ch.fhnw.cpthook.model.Lava
import ch.fhnw.cpthook.model.Npo
import ch.fhnw.cpthook.model.Position
import ch.fhnw.cpthook.model.Position
import ch.fhnw.cpthook.model.Size
import ch.fhnw.cpthook.model.Vec2.toVec3
import ch.fhnw.cpthook.viewmodel.ILevelViewModel
import ch.fhnw.ether.controller.IController
import ch.fhnw.ether.controller.event.IEventScheduler.IAnimationAction
import ch.fhnw.ether.controller.event.IKeyEvent
import ch.fhnw.ether.controller.event.IPointerEvent
import ch.fhnw.ether.controller.tool.AbstractTool
import ch.fhnw.ether.controller.tool.PickUtilities
import ch.fhnw.ether.controller.tool.PickUtilities.PickMode
import ch.fhnw.ether.scene.I3DObject
import ch.fhnw.ether.scene.camera.ICamera
import ch.fhnw.ether.scene.camera.IViewCameraState
import ch.fhnw.ether.scene.mesh.DefaultMesh
import ch.fhnw.ether.view.ProjectionUtilities
import ch.fhnw.util.math.Mat4
import ch.fhnw.util.math.Vec3
import javax.swing.JFileChooser
import ch.fhnw.cpthook.SoundManager

/**
 * Tool, which is used in the editor.
 * Responsible for movement and level changes (e.g. block adding).
 */


/**
 * Tool, which is used in the editor.
 * Responsible for movement and level changes (e.g. block adding).
 */
class EditorTool(val controller: ICptHookController, val camera: ICamera, val viewModel: ILevelViewModel)
  extends AbstractTool(controller) with IAnimationAction {

  val OffsetScale = 0.2f
  val GuiBlockSize = 0.5f
  val GuiBlockRotationAxis = new Vec3(0, 1, 0)

  var offsetX: Float = 0
  var startX: Float = 0
  var offsetZ: Float = 20
  var currentBlockRotation: Float = 0
  var currentBlockScale: Float = 1f
  @volatile var cameraNeedsUpdate: Boolean = true

  //Factories
  val blockFactory = Block(_, _)
  val lavaFactory = Lava(_, _)
  val iceFactory = Ice(_, _)

  //Tuples with npo factories and meshes
  //TODO: Remove unsafe cast
  val editorMeshes = List(
      (blockFactory, blockFactory(Position(0, 0),  Size(1, 1)).to3DObject.asInstanceOf[DefaultMesh]),
      (lavaFactory, lavaFactory(Position(0, 0),  Size(1, 1)).to3DObject.asInstanceOf[DefaultMesh]),
      (iceFactory, iceFactory(Position(0, 0),  Size(1, 1)).to3DObject.asInstanceOf[DefaultMesh])
  );
  editorMeshes foreach { mesh => mesh._2.setTransform(Mat4 scale GuiBlockSize) }

  object EditingState extends Enumeration { val Adding, Removing = Value }
  /** Determines, if the user is currently adding or removing elements. */
  var editingState = EditingState.Adding
  /** Current factory, which is used to add Npos. */
  var currentFactory: (Position, Size) => Npo = blockFactory
  /** Current size, which is used when adding Npos. */
  var currentSize = () => Size(1, 1) //Size is mutable, so it has to be a factory
  
  camera.setUp(Defaults.cameraUp)

  override def activate = {

    //Switch sounds
    //TODO: To slow loading.. has to be fixed
    //SoundManager.playSong(SoundManager.Ambient)

    editorMeshes.foreach { mesh => controller.getScene.add3DObject(mesh._2) }
    cameraNeedsUpdate = true
    controller.animate(this)
  }

  override def deactivate = {
    SoundManager.stopSong
    editorMeshes.foreach { mesh => controller.getScene.remove3DObject(mesh._2) }
    cameraNeedsUpdate = true
    controller.kill(this)
  }

  /**
   * Sets the camera position and value to the current offset information.
   */
  private def updateCamera : Unit = {
    camera.setTarget(new Vec3(offsetX, 0, 1))
    camera.setPosition(new Vec3(offsetX, 0, offsetZ))
  }

  /**
   * Update gui component positions.
   * So they seem fixed relative to the camera.
   */
  private def updateGuiPositions : Unit = {
    if(controller.getViews.isEmpty){ return }
    
    val view = controller.getViews.get(0)
    val viewport = view.getViewport
    val left = rayToXYPlane(0, viewport.h / 2, 10);
    val right = rayToXYPlane(viewport.w, viewport.h / 2, 10);
    
    val xFactor = (right.x - left.x) / viewport.w
    
    currentBlockScale = 70 * xFactor

    var i = 0
    editorMeshes.map(_._2).foreach { mesh =>
      mesh.setTransform(Mat4.multiply(Mat4.rotate(currentBlockRotation, 0, 1, 0), Mat4.scale(currentBlockScale)))
      mesh.setPosition(right.add(new Vec3(-100 * xFactor, i * 100 * xFactor, 0)))
      i += 1
    }
  }
  
  private def rayToXYPlane(xScreen: Float, yScreen: Float, zOffset: Float): Vec3 = {
    val view = controller.getViews.get(0)
    val viewCameraState = getController.getRenderManager.getViewCameraState(view)
    val ray = ProjectionUtilities.getRay(viewCameraState, xScreen, yScreen)
     // check if we can hit the xy plane
    if(ray.getDirection.z != 0f) {
      val s: Float = (-ray.getOrigin.z + zOffset) / ray.getDirection.z
      val p: Vec3 = ray.getOrigin add ray.getDirection.scale(s)
      return p
    }
    return Vec3.ZERO
  }

  /**
   * Check if a control was clicked and handle the clicking if this is the case.
   */
  private def clickedControl(event: IPointerEvent)(implicit viewCameraState: IViewCameraState): Boolean = {
    val hitDistance = (mesh: I3DObject) => this.hitDistance(mesh, event.getX, event.getY)
    val hits = editorMeshes
      .map(mesh => (hitDistance(mesh._2) -> mesh._1))
      .filter(_._1 < Float.PositiveInfinity)
    if(hits.isEmpty) { false }
    else {
      currentFactory = hits.minBy(_._1)._2
      true
    }
  }

  /**
   * Remove nearest clicked block. Returns true if a block was removed and false otherwise.
   */
  private def remove(event: IPointerEvent)(implicit viewCameraState: IViewCameraState): Boolean = {
    val npo = findNearest(event)
    if(npo != null){ viewModel.removeNpo(npo) }
    return npo != null
  }

  /**
   * Calculates the distance from a click location to a mesh.
   */
  private def hitDistance(mesh: I3DObject, x: Int, y: Int)(implicit viewCameraState: IViewCameraState) =
    PickUtilities.pickObject(PickMode.POINT, x, y, 0, 0, viewCameraState, mesh)

  /**
   * Finds nearest clicked block. If no block was clicked null is returned.
   */
  private def findNearest(event: IPointerEvent)(implicit viewCameraState: IViewCameraState): Npo = {
    val hitDistance = (mesh: I3DObject) => this.hitDistance(mesh, event.getX, event.getY)
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
      val p: Vec3 = rayToXYPlane(event.getX, event.getY, 0)
      val size = currentSize()
      viewModel.addNpo(currentFactory(new Position((p.x - size.x / 2).round.toInt, (p.y - size.y / 2).round.toInt), size))
  }
  
  override def pointerPressed(event: IPointerEvent): Unit = event.getButton match {
    case IPointerEvent.BUTTON_2 | IPointerEvent.BUTTON_3 =>
      startX = event.getX
      //Hide editor meshes while moving (because they are very jittery)
      editorMeshes.foreach { mesh => controller.getScene.remove3DObject(mesh._2) }
    case IPointerEvent.BUTTON_1 =>
      implicit val viewCameraState = getController.getRenderManager.getViewCameraState(event.getView)
      val control = clickedControl(event)
      if(!control){
        val removed = remove(event)
        if(!removed){ add(event) }
        editingState = if(removed) EditingState.Removing else EditingState.Adding
      }
    case default => //Unknown key
  }

  override def pointerReleased(event: IPointerEvent): Unit = event.getButton match {
    case IPointerEvent.BUTTON_2 | IPointerEvent.BUTTON_3 =>
      //Add editor meshes back after moving
      editorMeshes.foreach { mesh => controller.getScene.add3DObject(mesh._2) }
    case default => //Unknown key
  }

  /**
   * Moves the view horizontally in the editor mode.
   */
  override def pointerDragged(event: IPointerEvent): Unit = event.getButton match {
    case IPointerEvent.BUTTON_2 | IPointerEvent.BUTTON_3 =>
      val delta = event.getX - startX
      offsetX += delta * OffsetScale
      cameraNeedsUpdate = true
      startX = event.getX
    case IPointerEvent.BUTTON_1 =>
      implicit val viewCameraState = getController.getRenderManager.getViewCameraState(event.getView)
      if(editingState == EditingState.Adding){
        if(findNearest(event) == null){ add(event) }
      } else {
        remove(event)
      }
    case default => //Unknown key
  }

  override def pointerMoved(event: IPointerEvent): Unit = {
    //TODO: Ghost block
  }
  
  override def pointerScrolled(event: IPointerEvent): Unit = {
    offsetZ += event.getScrollY * OffsetScale
    if (offsetZ < 13) {
      offsetZ = 13
    } else if (offsetZ > 120) {
      offsetZ = 120
    }
    cameraNeedsUpdate = true
  }

  override def keyPressed(event: IKeyEvent): Unit = event.getKeyCode match {
    case KeyEvent.VK_M =>
      controller.setCurrentTool(new GameTool(controller, camera, viewModel))
    case KeyEvent.VK_S if event.isControlDown =>
      val fileChooser = new JFileChooser
      fileChooser.setDialogTitle("Save Level")
      if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
        viewModel.saveLevel(fileChooser.getSelectedFile.getAbsolutePath)
      }
    case KeyEvent.VK_O if event.isControlDown =>
      val fileChooser = new JFileChooser
      fileChooser.setDialogTitle("Open Level")
      if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
        viewModel.openLevel(fileChooser.getSelectedFile.getAbsolutePath)
      }
    case default => //Unknown key
  }

  /**
   * Animation of editor controls.
   */
  def run(time: Double, interval: Double) : Unit = {
    currentBlockRotation = (currentBlockRotation + 1) % 360
    
    if (cameraNeedsUpdate) {
      updateCamera
      updateGuiPositions
    }
    
    editorMeshes map {_._2} foreach { mesh =>
      mesh.setTransform(Mat4.multiply(Mat4.rotate(currentBlockRotation, 0, 1, 0), Mat4.scale(currentBlockScale)))
    }
  }

}