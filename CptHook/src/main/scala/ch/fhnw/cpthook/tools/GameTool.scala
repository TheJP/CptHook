package ch.fhnw.cpthook.tools

import ch.fhnw.cpthook.viewmodel.ILevelViewModel
import ch.fhnw.ether.controller.IController
import ch.fhnw.ether.controller.event.IPointerEvent
import ch.fhnw.ether.controller.tool.AbstractTool
import ch.fhnw.ether.controller.tool.PickUtilities
import ch.fhnw.ether.controller.tool.PickUtilities.PickMode
import ch.fhnw.ether.scene.camera.ICamera
import ch.fhnw.ether.scene.I3DObject
import ch.fhnw.cpthook.model.Entity
import ch.fhnw.ether.controller.event.IKeyEvent
import com.jogamp.newt.event.KeyEvent
import ch.fhnw.ether.controller.event.IEventScheduler.IAnimationAction
import org.jbox2d.dynamics.World
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import ch.fhnw.util.math.Vec3
import org.jbox2d.common.Vec2
import ch.fhnw.cpthook.model.SkyBox
import ch.fhnw.ether.scene.mesh.material.ColorMapMaterial
import ch.fhnw.cpthook.ICptHookController
import ch.fhnw.util.math.Mat4
import ch.fhnw.cpthook.SoundManager
import ch.fhnw.cpthook.model.EntitiyUpdatable
import ch.fhnw.cpthook.model.EntityActivatable
import ch.fhnw.cpthook.model.IGameStateChanger
import ch.fhnw.cpthook.model.IGameStateController
import ch.fhnw.ether.ui.Button
import ch.fhnw.ether.ui.Button.IButtonAction
import ch.fhnw.ether.view.IView
import ch.fhnw.cpthook.EtherHacks
import ch.fhnw.ether.scene.mesh.IMesh
import ch.fhnw.ether.ui.Slider
import ch.fhnw.ether.ui.Slider.ISliderAction
import ch.fhnw.cpthook.model.CheckpointBlock
import ch.fhnw.cpthook.model.Position
import scala.reflect.api.Position

/**
 * Tool, which handles the game logic.
 */
class GameTool(val controller: ICptHookController, val camera: ICamera, val viewModel: ILevelViewModel)
  extends AbstractTool(controller) with IAnimationAction with IGameStateController {
  
  val inputManager = controller.inputManager
  val world: World = new World(new org.jbox2d.common.Vec2(0.0f, -40.0f))
  val gameContactListener = new GameContactListener
  var follow = true
  val skyBox = new SkyBox().createMesh()
  var skyBoxOffsetX = 0.0
  var skyBoxOffsetY = 0.0
  var lastX = 0.0
  var lastY = 0.0
  var deathZone = 1000.0;
  
  var updateableEntites = List[EntitiyUpdatable]()
  var activatableEntites = List[EntityActivatable]()
  var stateChangerEntities = List[IGameStateChanger]()

  override def activate(): Unit = {

    //Switch sounds
    SoundManager.playSound(SoundManager.LevelSound, 0.2f, true, true)

    //TODO: Improve this
    //Set checkpoint position of player
    val position = viewModel.getPlayer.position
    val checkpoint = viewModel.getCheckpoint
    if(checkpoint != null){
      viewModel.getPlayer.position = Position(checkpoint.position.x + 1, checkpoint.position.y + 1)
    }

    //Link all entities to box2D
    (viewModel.entities.keys ++ Iterable(viewModel.getPlayer)) foreach { _.linkBox2D(world) }

    //Reset player position to level position
    viewModel.getPlayer.position = position

    (viewModel.entities.keys ++ Iterable(viewModel.getPlayer)) foreach { entity =>
      if (entity.isInstanceOf[EntitiyUpdatable]) {
        updateableEntites ::= entity.asInstanceOf[EntitiyUpdatable]
      }
      if (entity.isInstanceOf[EntityActivatable]) {
        activatableEntites ::= entity.asInstanceOf[EntityActivatable]
      }
      if (entity.isInstanceOf[IGameStateChanger]) {
        stateChangerEntities ::= entity.asInstanceOf[IGameStateChanger]
      }
    }
    
    world.setContactListener(gameContactListener)

    stateChangerEntities foreach { _.init(this) }
    activatableEntites foreach { _.activate(gameContactListener) }

    //Skybox
    viewModel.addSkyBox(skyBox)
    lastX = viewModel.getPlayer.mesh.getPosition.x
    lastY = viewModel.getPlayer.mesh.getPosition.y
    
    //calculate death zone
    viewModel.entities.foreach((entity : (Entity, IMesh)) => deathZone = Math.min(deathZone, entity._2.getPosition.y))
    deathZone -= 10
    
    controller.animate(this)
    
    setupUI()
  }
  
  def setupUI(): Unit = {
    val volumeControle = new Slider(0, 1, "Volume", "Volume of the game", SoundManager.getVolumeAdjustment(), new ISliderAction() {
      def execute(slider: Slider, view: IView): Unit =  {
        SoundManager.volumeAdjust(slider.getValue)
      }
    })
        
    val switchModeButton = new Button(0, 2, "Edit", "(M) Switches to edit mode", KeyEvent.VK_M, new IButtonAction() {
      def execute(button: Button, view: IView) = {
        EtherHacks.removeWidgets(controller)
        controller.setCurrentTool(new EditorTool(controller, camera, viewModel)) 
      }
    })

    controller.getUI.addWidget(switchModeButton)
    controller.getUI.addWidget(volumeControle)
  }
  
  override def deactivate: Unit = {
    SoundManager.stopAll
    viewModel.removeSkyBox(skyBox)
    deathZone = 1000.0f;
    controller.kill(this)
    activatableEntites.foreach { _.deactivate }
  }

  def run(time: Double, interval: Double) : Unit = {
    world.step(interval.toFloat, GameTool.VelocityIterations, GameTool.PositionIterations)
    
    if(viewModel.getPlayer.mesh.getPosition.y < deathZone) gameOver
    
    updateableEntites.foreach { _.update(inputManager, time) }
    
    updateCamera
    updateSkyBox
   
    inputManager.clearWasPressed
  }
  
  def updateCamera: Unit = {
    camera.setTarget(viewModel.getPlayer.mesh.getPosition)
    if(follow) {
      camera.setPosition(viewModel.getPlayer.mesh.getPosition.add(new Vec3(0, 0, 20)))
    }   
  }

  def updateSkyBox: Unit = {
    if (follow) {
      skyBoxOffsetX += ((viewModel.getPlayer.mesh.getPosition.x - lastX) * 0.5)
      skyBoxOffsetY += ((viewModel.getPlayer.mesh.getPosition.y - lastY) * 0.5)
      lastX = viewModel.getPlayer.mesh.getPosition.x
      lastY = viewModel.getPlayer.mesh.getPosition.y
      skyBox.setPosition(viewModel.getPlayer.mesh.getPosition.subtract(new Vec3(skyBoxOffsetX, skyBoxOffsetY, 20)))  
      if(skyBoxOffsetX > 60) skyBoxOffsetX = -60
      else if(skyBoxOffsetX < -60) skyBoxOffsetX = 60  
      if(skyBoxOffsetY > 60) skyBoxOffsetY = -60
      else if(skyBoxOffsetY < -60) skyBoxOffsetY = 60 
    }
  }
 
  override def keyPressed(event: IKeyEvent): Unit = event.getKeyCode match {
      
    case KeyEvent.VK_F =>
      follow = !follow
      if(!follow) { camera.setPosition(new Vec3(0, 0, 20)) }
      
    case _ =>

  }

  //Game Over and win sounds?
  def isActive = controller.getCurrentTool == this
  def gameOver: Unit = if(isActive){ 
    viewModel.getPlayer.onGroundCount = 0
    viewModel.getPlayer.jumpCount = 0
    controller.setCurrentTool(new GameTool(controller, camera, viewModel)) 
  }
  def killMonser(body: Body): Unit = {} //TODO:
  def win: Unit = if(isActive){ 
    EtherHacks.removeWidgets(controller)
    controller.setCurrentTool(new EditorTool(controller, camera, viewModel)) 
  }
  def switchGravity = world.setGravity(world.getGravity.mul(-1f))
  def setCheckpoint(point: CheckpointBlock) = viewModel.setCheckpoint(point)
}

object GameTool {
  val VelocityIterations: Int = 6
  val PositionIterations: Int = 3
}
