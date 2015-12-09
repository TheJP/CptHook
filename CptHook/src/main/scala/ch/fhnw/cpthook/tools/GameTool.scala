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

/**
 * Tool, which handles the game logic.
 */
class GameTool(val controller: IController, val camera: ICamera, val viewModel: ILevelViewModel)
  extends AbstractTool(controller) with IAnimationAction {
  
  var playerBody: Body = null
  val velocityIterations: Int = 6
  val positionIterations: Int = 3
  
  val world: World = new World(new org.jbox2d.common.Vec2(0.0f, -10.0f))
  
  override def activate(): Unit = {
    
    viewModel.npos.keys.map {npo => (npo.toBox2D, npo)} foreach { definition =>
      val body: Body = world.createBody(definition._1._1)
      body.createFixture(definition._1._2)
      body.setUserData(definition._2)
    }
    
    // special player handling
    val playerBodyDef = new BodyDef
    playerBodyDef.position.set(viewModel.getPlayer.position.x, viewModel.getPlayer.position.y)
    playerBodyDef.`type` = BodyType.DYNAMIC
    playerBodyDef.fixedRotation = true
    
    val playerShape: PolygonShape = new PolygonShape
    playerShape.setAsBox(0.5f, 0.5f);
    
    val playerFixtureDef: FixtureDef = new FixtureDef
    playerFixtureDef.shape = playerShape
    playerFixtureDef.friction = 0.2f;        
    playerFixtureDef.restitution = 0f;
    playerFixtureDef.density = 1f;
    
    playerBody = world.createBody(playerBodyDef)
    playerBody.createFixture(playerFixtureDef)
    playerBody.setUserData(viewModel.getPlayer)
    
    
   controller.animate(this)
  }
  
  override def deactivate(): Unit = {
   controller.kill(this)
  }
  
  def run(time: Double, interval: Double) : Unit = {
    world.step(1f / 60f, velocityIterations, positionIterations)
    
    println(new Vec3(playerBody.getPosition.x, playerBody.getPosition.y, 0))
    
    viewModel.getPlayer3DObject.setPosition(new Vec3(playerBody.getPosition.x, playerBody.getPosition.y, 0))
  }
 
  override def keyPressed(event: IKeyEvent): Unit = event.getKeyCode match {

    case KeyEvent.VK_M =>
       println("switching to editor mode")
        controller.setCurrentTool(new EditorTool(controller, camera, viewModel))

    case KeyEvent.VK_RIGHT =>
      val velocity = playerBody.getLinearVelocity
      playerBody.setLinearVelocity(velocity.add(new Vec2(1f, 0f)))

    case KeyEvent.VK_LEFT =>
      val velocity = playerBody.getLinearVelocity
      playerBody.setLinearVelocity(velocity.add(new Vec2(-1f, 0f)))

    case KeyEvent.VK_SPACE =>
      val velocity = playerBody.getLinearVelocity
      playerBody.setLinearVelocity(velocity.add(new Vec2(0f, 5f)))

    case default =>
      println(s"key $default does nothing")

  }
  
}