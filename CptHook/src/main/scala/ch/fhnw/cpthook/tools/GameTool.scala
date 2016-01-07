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
import ch.fhnw.cpthook.model.SkyBox
import ch.fhnw.ether.scene.mesh.material.ColorMapMaterial
import ch.fhnw.cpthook.ICptHookController
import ch.fhnw.util.math.Mat4

/**
 * Tool, which handles the game logic.
 */
class GameTool(val controller: ICptHookController, val camera: ICamera, val viewModel: ILevelViewModel)
  extends AbstractTool(controller) with IAnimationAction {
  
  val inputManager = controller.inputManager
  val world: World = new World(new org.jbox2d.common.Vec2(0.0f, -40.0f))
  val gameContactListener = new GameContactListener
  var follow = true
  val skyBox = new SkyBox().createMesh()
  
  override def activate(): Unit = {
    
    // create toBox2D models for blocks
    viewModel.npos.keys.map {npo => (npo.toBox2D, npo)} foreach { definition =>
      val body: Body = world.createBody(definition._1._1)
      body.createFixture(definition._1._2)
      body.setUserData(definition._2)
    }

    // link user to box2D
    viewModel.getPlayer.linkBox2D(world)
    
    // register contact listener
    world.setContactListener(gameContactListener)
    
    // register player update listener (Small hack here. Be aware that if you add a new fixture to the player
    // this will no longer work!)
    gameContactListener.register(viewModel.getPlayer, viewModel.getPlayer.body.getFixtureList)
    
    
    //Skybox
    updateSkyBox()
    viewModel.addSkyBox(skyBox)
    
    controller.animate(this)
  }
  
  def updateSkyBox(): Unit = {
    var minX = 0f;
    var maxX = 0f;
    var minY = 0f;
    var maxY = 0f;
    viewModel.npos.map {_._2} foreach { mesh => 
      if (mesh.getPosition.x < minX) {
        minX = mesh.getPosition.x
      }
      if (mesh.getPosition.x > maxX) {
        maxX = mesh.getPosition.x
      }
      if (mesh.getPosition.y < minX) {
        minY = mesh.getPosition.y
      }
      if (mesh.getPosition.y > maxY) {
        maxY = mesh.getPosition.y
      }
    }
    var skyBoxScale = Math.max(maxX - minX, maxY - minY)
    skyBoxScale *= 1.2f
    if (skyBoxScale < 40f) {
      skyBoxScale = 40f
    }
    skyBox.setPosition(new Vec3((maxX - minX) / 2f, (maxY - minY) / 2f, -20))
    skyBox.setTransform(Mat4.scale(skyBoxScale))
  }
  
  override def deactivate(): Unit = {
    viewModel.removeSkyBox(skyBox)
    controller.kill(this)
    viewModel.getPlayer.unlinkBox2D(world)
  }

  def run(time: Double, interval: Double) : Unit = {
    world.step(interval.toFloat, GameTool.VelocityIterations, GameTool.PositionIterations)
    
    viewModel.getPlayer.update(inputManager, time)
    
    camera.setTarget(viewModel.getPlayer.mesh.getPosition)
    if(follow) {
      camera.setPosition(viewModel.getPlayer.mesh.getPosition.add(new Vec3(0, 0, 20)))
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