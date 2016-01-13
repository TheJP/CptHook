package ch.fhnw.cpthook.tools

import com.jogamp.newt.event.KeyEvent
import ch.fhnw.cpthook.Defaults
import ch.fhnw.cpthook.ICptHookController
import ch.fhnw.cpthook.model.GrassBlock
import ch.fhnw.cpthook.model.Entity
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
import javax.swing.SwingUtilities
import ch.fhnw.ether.controller.event.IEventScheduler.IAction
import ch.fhnw.ether.scene.mesh.IMesh
import ch.fhnw.cpthook.model._
import javafx.scene.media.AudioClip
import javafx.scene.media.MediaPlayer
import javafx.scene.media.Media
import ch.fhnw.ether.ui.Button
import ch.fhnw.ether.ui.Button.IButtonAction
import ch.fhnw.ether.view.IView
import ch.fhnw.cpthook.LevelLoader
import ch.fhnw.cpthook.EtherHacks
import ch.fhnw.ether.scene.mesh.material.LineMaterial
import ch.fhnw.util.color.RGBA
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.Primitive
import scala.collection.mutable.MutableList
import ch.fhnw.ether.scene.mesh.material.ColorMaterial
import ch.fhnw.ether.scene.mesh.IMesh.Queue
import javafx.scene.shape.Mesh
import ch.fhnw.ether.ui.Slider
import ch.fhnw.ether.ui.Slider.ISliderAction

/**
 * Tool, which is used in the editor.
 * Responsible for movement and level changes (e.g. block adding).
 */
class EditorTool(val controller: ICptHookController, val camera: ICamera, val viewModel: ILevelViewModel)
  extends AbstractTool(controller) with IAnimationAction {

  val OffsetScale = 0.2f
  val GuiBlockSize = 0.5f
  val GuiBlockRotationAxis = new Vec3(0, 1, 0)
  val GridSize = (1000, 400)

  var offsets = new Vec3(0, 0, 20)
  var dragStart = (0f, 0f)
  var currentBlockRotation: Float = 0
  var currentBlockScale: Float = 1f
  var gridMesh: IMesh = null
  var selectMesh: IMesh = null
  @volatile var cameraNeedsUpdate: Boolean = true

  type EntityFactory = (Position, Size) => Entity
  
  // Tuples with entities that can be added
  val editorMeshes = List(
    ((p, s) => new GrassBlock(p, s), "Nice green grass"),
    ((p, s) => new DirtBlock(p, s), "Dirt"),
    ((p, s) => new IceBlock(p, s), "Ice with gliding properties"),
    ((p, s) => new LavaBlock(p, s), "Lava.. gotta roast some captains"),
    ((p, s) => new TargetBlock(p, s), "Target: You win when you touch it"),
    ((p, s) => new TrampolineBlock(p, s), "Bounce, Bounce, Bounce"),
    ((p: Position, s: Size) => new Monster(p), "Monster.. it doesn't like to be touched"),
    ((p, s) => new GravityBlock(p, s), "Turn the world upside down.. literally")
  ) map { npo => (npo._1(Position(0, 0), Size(1, 1)).toMesh, npo._1, npo._2) } reverse

  object EditingState extends Enumeration { val Adding, Removing = Value }
  /** Determines, if the user is currently adding or removing elements. */
  var editingState = EditingState.Adding
  /** Current factory, which is used to add Npos. */
  var currentFactory: EntityFactory = editorMeshes.head._2
  /** Current size, which is used when adding Npos. */
  var currentSize = () => Size(1, 1) //Size is mutable, so it has to be a factory
  
  camera.setUp(Defaults.cameraUp)

  override def activate = {

    //Switch sounds
    SoundManager.playSound(SoundManager.AmbientSound, 0.8f, true, true)
    
    addEditorMeshes
    cameraNeedsUpdate = true
    controller.animate(this)
    
    setupUI
    setupGrid
    setupSelection
  }

  override def deactivate = {
    SoundManager.stopAll
    removeEditorMeshes
    
    if (gridMesh != null) { getController.getScene.remove3DObject(gridMesh) }
    if (selectMesh != null) { getController.getScene.remove3DObject(selectMesh) }
    
    cameraNeedsUpdate = true
    controller.kill(this)
  }
  
  private def setupUI: Unit = {
    val exitButton = new Button(0, 0, "Exit", "(Q) Closes the game",  KeyEvent.VK_Q, new IButtonAction() {
      def execute(button: Button, view: IView) = {
        System.exit(0) //TODO: Ask to save (also when pressing esc) and graceful shutdown
      }
    })

    val switchModeButton = new Button(0, 1, "Play", "(M) Switches to play mode", KeyEvent.VK_M, new IButtonAction() {
      def execute(button: Button, view: IView) = {
        EtherHacks.removeWidgets(controller)
        controller.setCurrentTool(new GameTool(controller, camera, viewModel))   
      }
    })
    
    val volumeControle = new Slider(0, 2, "Volume", "Volume of the game", SoundManager.getVolumeAdjustment(), new ISliderAction() {
      def execute(slider: Slider, view: IView): Unit =  {
        SoundManager.volumeAdjust(slider.getValue)
      }
    })
   
    val clearButton = new Button(0, 3, "Clear", "(C) Clears the current level",  KeyEvent.VK_Q, new IButtonAction() {
      def execute(button: Button, view: IView) = {
        val oldLevel = viewModel.getLevel
        viewModel.loadLevel(new Level(oldLevel.size, oldLevel.start, List()))
      }
    })

    val loadLevelButton = new Button(0, 4, "Open", "(O) Open level from file", KeyEvent.VK_O, new IButtonAction() {
      def execute(button: Button, view: IView) = {
        var level = LevelLoader.loadFromFile()
        if (level != null) {
          viewModel.loadLevel(level)
        }
      }
    })
    
    val saveLevelButton = new Button(0, 5, "Save", "(S) Save level to file", KeyEvent.VK_S, new IButtonAction() {
      def execute(button: Button, view: IView) = { LevelLoader.saveToFile(viewModel.getLevel) }
    })
    
    val browseLevelButton = new Button(0, 6, "Browse", "(B) Browse levels from server", KeyEvent.VK_B, new IButtonAction() {
      def execute(button: Button, view: IView) = {
        var level = LevelLoader.loadFromServer()
        if (level != null) {
          viewModel.loadLevel(level)
        }
      }
    })
    
    val uploadLevelButton = new Button(0, 7, "Upload", "(U) Upload level to server", KeyEvent.VK_U, new IButtonAction() {
      def execute(button: Button, view: IView) = { LevelLoader.pushToServer(viewModel.getLevel) }
    })

    controller.getUI.addWidget(exitButton)
    controller.getUI.addWidget(switchModeButton)
    controller.getUI.addWidget(volumeControle)
    controller.getUI.addWidget(clearButton)
    controller.getUI.addWidget(loadLevelButton)
    controller.getUI.addWidget(saveLevelButton)
    controller.getUI.addWidget(browseLevelButton)
    controller.getUI.addWidget(uploadLevelButton)

  }

  private def setupGrid: Unit = {
    val material = new LineMaterial(RGBA.BLACK)
    val halfX = GridSize._1 / 2
    val halfY = GridSize._2 / 2
    
    val vertices = MutableList[Float]()
    
    // horizontal
    for (y <- 0 to GridSize._2) {
      vertices += (-halfX, halfY - y, 0, halfX, halfY - y, 0)
    }
    
    // vertical
    for (x <- 0 to GridSize._1) {
      vertices += (halfX - x, -halfY, 0, halfX - x, halfY, 0)
    }
    
    val geometry = DefaultGeometry.createV(Primitive.LINES, vertices.toArray)
    gridMesh = new DefaultMesh(material, geometry)
    getController.getScene.add3DObject(gridMesh)
  }
  
  private def setupSelection(): Unit = {
    val material = new ColorMaterial(new RGBA(0f, 1f, 0f, 0.3f), false)
    val vertices = Array[Float](
        0, 0, 0,
        0, -1, 0,
        1, 0, 0,
        1, 0, 0,
        1, -1, 0,
        0, -1, 0
    )
    val geometry = DefaultGeometry.createV(Primitive.TRIANGLES, vertices)
    selectMesh = new DefaultMesh(material, geometry, Queue.TRANSPARENCY)
    getController.getScene.add3DObject(selectMesh)
  }
  
  /**
   * Sets the camera position and value to the current offset information.
   */
  private def updateCamera : Unit = {
    camera.setTarget(new Vec3(offsets.x, offsets.y, 0))
    camera.setPosition(offsets)
  }
  
  /**
   * Adds all editor meshes
   */
  private def addEditorMeshes: Unit =  {
     editorMeshes.foreach { editorMesh => controller.getScene.add3DObject(editorMesh._1) }
  }
  
  /**
   * Removes all editor meshes
   */
  private def removeEditorMeshes: Unit =  {
     editorMeshes.foreach { editorMesh => controller.getScene.remove3DObject(editorMesh._1) }
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
    
    currentBlockScale = viewport.h * 0.05f * xFactor
    
    var offset = editorMeshes.size * currentBlockScale / 2f

    var i = 0
    editorMeshes.map(_._1).foreach { mesh =>
      mesh.setTransform(Mat4.multiply(Mat4.rotate(currentBlockRotation, 0, 1, 0), Mat4.scale(currentBlockScale)))
      mesh.setPosition(right.add(new Vec3(-1.5 * currentBlockScale, i * 1.5 * currentBlockScale - offset, 0)))
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
   * Remove nearest clicked block. Returns true if a block was removed and false otherwise.
   */
  private def remove(event: IPointerEvent): Boolean = {
    val entity = findEntityAtPosition(event)
    if (entity.isDefined) {
      viewModel.removeNpo(entity.get)
      return true
    }
    return false
  }
  
  /**
   * Checks whether the field under the cursor is already containing an entity
   */
  private def findEntityAtPosition(event: IPointerEvent): Option[Entity] = {
    val p = rayToXYPlane(event.getX, event.getY, 0)
    val pos = roundVec3(p)

    return viewModel.entities.map(_._1).find(_.position == pos)
  }

  /**
   * Adds a block at the pointer position if possible.
   */
  private def add(event: IPointerEvent): Unit = {
      val p = rayToXYPlane(event.getX, event.getY, 0)
      val size = currentSize()
      viewModel.addNpo(currentFactory(roundVec3(p), size))
  }
  
  override def pointerPressed(event: IPointerEvent): Unit = event.getButton match {
    case IPointerEvent.BUTTON_2 | IPointerEvent.BUTTON_3 =>
      dragStart = (event.getX, event.getY)
      //Hide editor meshes while moving (because they are very jittery)
      removeEditorMeshes
    case IPointerEvent.BUTTON_1 =>
      implicit val viewCameraState = getController.getRenderManager.getViewCameraState(event.getView)
      val control = hitControl(event)
      if (control.isDefined) {
        currentFactory = control.get._1
        return
      }
      if (findEntityAtPosition(event).isDefined) {
        remove(event)
        editingState = EditingState.Removing
      } else {
        add(event)
        editingState = EditingState.Adding
      }
    case default => //Unknown key
  }
  
 /**
   * Check if a control was clicked and handle the clicking if this is the case.
   */
  private def hitControl(event: IPointerEvent) (implicit viewCameraState: IViewCameraState): Option[(EntityFactory, String)] = {
    val hitDistance = (mesh: I3DObject) => this.hitDistance(mesh, event.getX, event.getY)
    val hits = editorMeshes
      .map(mesh => (hitDistance(mesh._1) -> (mesh._2, mesh._3)))
      .filter(_._1 < Float.PositiveInfinity)
    if(hits.isEmpty) { None }
    else {
      Option(hits.minBy(_._1)._2)
    }
  }
  
  /**
   * Calculates the distance from a click location to a mesh.
   */
  private def hitDistance(mesh: I3DObject, x: Int, y: Int)(implicit viewCameraState: IViewCameraState) =
    PickUtilities.pickObject(PickMode.POINT, x, y, 0, 0, viewCameraState, mesh)


  override def pointerReleased(event: IPointerEvent): Unit = event.getButton match {
    case IPointerEvent.BUTTON_2 | IPointerEvent.BUTTON_3 =>
      //Add editor meshes back after moving
      addEditorMeshes
    case default => //Unknown key
  }

  /**
   * Moves the view horizontally in the editor mode.
   */
  override def pointerDragged(event: IPointerEvent): Unit = event.getButton match {
    case IPointerEvent.BUTTON_2 | IPointerEvent.BUTTON_3 =>
      val deltaX = event.getX - dragStart._1
      val deltaY = event.getY - dragStart._2
      offsets = offsets.add(new Vec3(deltaX * OffsetScale, deltaY * OffsetScale, 0))
      cameraNeedsUpdate = true
      dragStart = (event.getX, event.getY)
    case IPointerEvent.BUTTON_1 =>
      if(editingState == EditingState.Adding){
        if(findEntityAtPosition(event).isEmpty){ add(event) }
      } else {
        remove(event)
      }
    case default => //Unknown key
  }

  override def pointerMoved(event: IPointerEvent): Unit = {
    //Show ui text
    implicit val viewCameraState = getController.getRenderManager.getViewCameraState(event.getView)
    val control = hitControl(event)
    if(control.isDefined){
      controller.getUI.setMessage(control.get._2)
    }
    //Move grid position marker
    val p: Vec3 = rayToXYPlane(event.getX, event.getY, 0)
    val pos = roundVec3(p)
    if (pos.x != selectMesh.getPosition.x || pos.y != selectMesh.getPosition.y) {
      selectMesh.setPosition(new Vec3(pos.x, pos.y, 0))
    }  
  }
  
  private def roundVec3(v: Vec3): Position = {
    new Position(Math.floor(v.x).toInt, Math.ceil(v.y).toInt)
  }
  
  override def pointerScrolled(event: IPointerEvent): Unit = {
    offsets = offsets.add(new Vec3(0, 0, event.getScrollY * OffsetScale))
    if (offsets.z < 13) {
      offsets = new Vec3(offsets.x, offsets.y, 13)
    } else if (offsets.z > 120) {
      offsets = new Vec3(offsets.x, offsets.y, 120)
    }
    cameraNeedsUpdate = true
  }

  override def keyPressed(event: IKeyEvent): Unit = {}

  /**
   * Animation of editor controls.
   */
  def run(time: Double, interval: Double) : Unit = {
    currentBlockRotation = (currentBlockRotation + 1) % 360

    if (cameraNeedsUpdate) {
      updateCamera
      updateGuiPositions
    }

    editorMeshes map { _._1 } foreach { mesh =>
      mesh.setTransform(Mat4.multiply(Mat4.rotate(currentBlockRotation, 0, 1, 0), Mat4.scale(currentBlockScale)))
    }
  }

}