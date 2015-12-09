package ch.fhnw.cpthook.tools

import ch.fhnw.cpthook.viewmodel.ILevelViewModel
import ch.fhnw.ether.controller.IController
import ch.fhnw.ether.controller.event.IPointerEvent
import ch.fhnw.ether.controller.tool.AbstractTool
import ch.fhnw.ether.controller.tool.PickUtilities
import ch.fhnw.ether.controller.tool.PickUtilities.PickMode
import ch.fhnw.ether.scene.camera.ICamera
import ch.fhnw.ether.scene.I3DObject
import ch.fhnw.cpthook.model.Npo
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
import ch.fhnw.cpthook.CptHookController

/**
 * Tool, which handles the game logic.
 */
class GameTool(val controller: IController, val camera: ICamera, val viewModel: ILevelViewModel)
  extends AbstractTool(controller) with IAnimationAction {
  
  val inputManager = controller.asInstanceOf[CptHookController].inputManager
  val world: World = new World(new org.jbox2d.common.Vec2(0.0f, -10.0f))
  var follow = true
  
  override def activate(): Unit = {
    
    viewModel.npos.keys.map {npo => (npo.toBox2D, npo)} foreach { definition =>
      val body: Body = world.createBody(definition._1._1)
      body.createFixture(definition._1._2)
      body.setUserData(definition._2)
    }

    viewModel.getPlayer.linkBox2D(world)
    
    controller.animate(this)
  }
  
  override def deactivate(): Unit = {
    controller.kill(this)
    viewModel.getPlayer.mesh.setPosition(viewModel.getPlayer.position toVec3 1)
  }
  
  def run(time: Double, interval: Double) : Unit = {
    world.step(1f / 60f, GameTool.VelocityIterations, GameTool.PositionIterations)
    
    viewModel.getPlayer.update(inputManager)
    
    camera.setTarget(viewModel.getPlayer.mesh.getPosition)
    if(follow) {
      camera.setPosition(viewModel.getPlayer.mesh.getPosition.subtract(new Vec3(0, 0, -20)))
    }   
    
    inputManager.clearWasPressed()
  }
 
  override def keyPressed(event: IKeyEvent): Unit = event.getKeyCode match {

    case KeyEvent.VK_M =>
       println("switching to editor mode")
        controller.setCurrentTool(new EditorTool(controller, camera, viewModel))
      
    case KeyEvent.VK_G =>
      world.setGravity(world.getGravity.mul(-1f))
      
    case KeyEvent.VK_F =>
      follow = !follow
      if(!follow) {
        camera.setPosition(new Vec3(0, 0, 20))
      }
      
    case _ =>

  }
  
}

object GameTool {
  val VelocityIterations: Int = 6
  val PositionIterations: Int = 3
}