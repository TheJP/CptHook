package ch.fhnw.cpthook.model

import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.BodyDef
import ch.fhnw.ether.scene.I3DObject
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.dynamics.BodyType
import com.jogamp.newt.event.KeyEvent
import ch.fhnw.util.math.Vec3
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.World
import ch.fhnw.ether.scene.mesh.material.ShadedMaterial
import ch.fhnw.ether.scene.mesh.MeshUtilities
import ch.fhnw.util.color.RGB
import ch.fhnw.cpthook.InputManager
import ch.fhnw.ether.image.Frame
import ch.fhnw.ether.scene.mesh.material.ColorMapMaterial
import ch.fhnw.ether.scene.mesh.DefaultMesh
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.Primitive
import ch.fhnw.ether.scene.mesh.IMesh.Queue;
import ch.fhnw.util.color.RGBA

class Player(var position: Position) {
  
  var body: Body = null
  val mesh: I3DObject = Player.createMesh(this)
  
  def linkBox2D(world: World): Unit = {
    val bodyDef = new BodyDef
    bodyDef.position.set(position.x, position.y)
    bodyDef.`type` = BodyType.DYNAMIC
    bodyDef.fixedRotation = true
    
    val shape: PolygonShape = new PolygonShape
    shape.setAsBox(0.45f, 0.45f);
    
    val fixtureDef: FixtureDef = new FixtureDef
    fixtureDef.shape = shape
    fixtureDef.friction = 0.1f;        
    fixtureDef.restitution = 0.1f;
    fixtureDef.density = 1f;
    
    body = world.createBody(bodyDef)
    body.createFixture(fixtureDef)
    body.setUserData(this)
  }
  
  def unlinkBox2D(world: World): Unit = {
    body = null
  }
  
  def update(inputManager: InputManager): Unit = {
    
    if (body == null) {
      return
    }
    
    mesh.setPosition(new Vec3(body.getPosition.x, body.getPosition.y,0.5))
    
    if (inputManager.keyPressed(KeyEvent.VK_RIGHT)) {
      val velocity = body.getLinearVelocity
      body.setLinearVelocity(velocity.add(new org.jbox2d.common.Vec2(Player.MoveVelocity, 0f)))
    }
    if (inputManager.keyPressed(KeyEvent.VK_LEFT)) {
      val velocity = body.getLinearVelocity
      body.setLinearVelocity(velocity.add(new org.jbox2d.common.Vec2(-Player.MoveVelocity, 0f)))
    }
    if (inputManager.keyWasPressed(KeyEvent.VK_SPACE)) {
      val velocity = body.getLinearVelocity
      if (body.getWorld.getGravity.y < 0) {
        body.setLinearVelocity(velocity.add(new org.jbox2d.common.Vec2(0f, Player.JumpVelocity)))
      } else {
        body.setLinearVelocity(velocity.add(new org.jbox2d.common.Vec2(0f, -Player.JumpVelocity)))
      }
      
    }

    val velocity = body.getLinearVelocity
    if (Math.abs(velocity.x) > Player.MaxXVelocity) {
      if(velocity.x > 0) {
         body.setLinearVelocity(new org.jbox2d.common.Vec2(Player.MaxXVelocity, velocity.y))
      } else {
         body.setLinearVelocity(new org.jbox2d.common.Vec2(-Player.MaxXVelocity, velocity.y))
      }
    }
  }
}

object Player {
  val MaxXVelocity: Float = 10.0f
  val MoveVelocity: Float = 1.0f
  val JumpVelocity: Float = 7.0f
  val z = 0f;
  val e = .5f;
  val texCoords = Array( 0f, 0f, 1f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 1f )
  val vertices = Array( -e, -e, z, e, -e, z, e, e, z, -e, -e, z, e, e, z, -e, e, z )
  
  val materialPlayer = new ColorMapMaterial(Frame.create(getClass.getResource("..\\assets\\player.png")).getTexture())
  val g = DefaultGeometry.createVM(Primitive.TRIANGLES, vertices, texCoords);
  
  def createMesh(player: Player): I3DObject = {

    val mesh = new DefaultMesh(materialPlayer, g, Queue.TRANSPARENCY);
    mesh.setPosition(player.position toVec3 0.5f)
    mesh
  }
}