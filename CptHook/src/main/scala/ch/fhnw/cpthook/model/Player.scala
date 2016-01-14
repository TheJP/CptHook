package ch.fhnw.cpthook.model

import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.Fixture
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.World
import org.jbox2d.dynamics.contacts.Contact
import com.jogamp.newt.event.KeyEvent
import ch.fhnw.cpthook.InputManager
import ch.fhnw.cpthook.tools.ContactUpdates
import ch.fhnw.ether.image.Frame
import ch.fhnw.ether.scene.mesh.DefaultMesh
import ch.fhnw.ether.scene.mesh.IMesh
import ch.fhnw.ether.scene.mesh.IMesh.Queue
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.Primitive
import ch.fhnw.ether.scene.mesh.material.ColorMapMaterial
import ch.fhnw.util.math.Vec3
import ch.fhnw.util.math.Mat4
import ch.fhnw.cpthook.SoundManager
import ch.fhnw.cpthook.tools.GameContactListener

/**
 * TODO: Comment this shizzle
 */
class Player(var position: Position) extends Entity with EntitiyUpdatable
                                                    with EntityActivatable
                                                    with ContactUpdates {

  import Player._
  
  var body: Body = null
  var jumpCount: Integer = 0
  var onGroundCount = 0
  var onTopCount = 0
  var stepAnimation: Integer = 0
  val mesh: IMesh = Player.createMesh(this)
  val walkingAnimation = (1 to 7).map { n => 
    new ColorMapMaterial(Frame.create(getClass.getResource(s"../assets/step$n.png")).getTexture())
  }.toArray
  
  val materialPlayer = new ColorMapMaterial(Frame.create(getClass.getResource("../assets/player.png")).getTexture())
  val materialPlayerJump = new ColorMapMaterial(Frame.create(getClass.getResource("../assets/step5.png")).getTexture())
  var timeOfAnimation: Double = 0.0
  var currentRotation = 0.0f
  var verticalRotation = 0.0f
  var currentDirection: Integer = 0
  
  
  var groundSensor: Fixture = null
  var topSensor: Fixture = null
  
  def toMesh: IMesh = Player.createMesh(this)
  
  def linkBox2D(world: World): Unit = {
    val bodyDef = new BodyDef
    bodyDef.position.set(position.x, position.y)
    bodyDef.`type` = BodyType.DYNAMIC
    bodyDef.fixedRotation = true
    
    val shape: PolygonShape = new PolygonShape
    shape.set(Player.createPlayerPolygon(0.3f, 0.7f, 0.1f), 8)
    val fixtureDef: FixtureDef = new FixtureDef
    fixtureDef.shape = shape
    fixtureDef.friction = 0.1f;        
    fixtureDef.restitution = 0.1f;
    fixtureDef.density = 200000000000f;
        
    val groundSensorShape: PolygonShape = new PolygonShape
    groundSensorShape.setAsBox(0.2f, 0.2f, new org.jbox2d.common.Vec2(0f, -0.7f), 0f)
    val groundSensorFixtureDef: FixtureDef = new FixtureDef
    groundSensorFixtureDef.shape = groundSensorShape
    groundSensorFixtureDef.isSensor = true
    
    val topSensorShape: PolygonShape = new PolygonShape
    topSensorShape.setAsBox(0.2f, 0.2f, new org.jbox2d.common.Vec2(0f, 0.7f), 0f)
    val topSensorFixtureDef: FixtureDef = new FixtureDef
    topSensorFixtureDef.shape = topSensorShape
    topSensorFixtureDef.isSensor = true
    
    body = world.createBody(bodyDef)
    body.createFixture(fixtureDef)
    groundSensor = body.createFixture(groundSensorFixtureDef)
    topSensor = body.createFixture(topSensorFixtureDef)
    body.setUserData(this)
  }
  
  def activate(gameContactListener: GameContactListener): Unit = {
    jumpCount = 0
    onGroundCount = 0
    onTopCount = 0
    // register player update listener (Small hack here. Be aware that if you add a new fixture to the player
    // this will no longer work!)
    gameContactListener.register(this, groundSensor)
    gameContactListener.register(this, topSensor)
  }
  
  def deactivate(): Unit =  {
    body = null
    mesh.setTransform(Mat4.ID)
    currentRotation = 0
    verticalRotation = 0
    currentDirection = 0
    mesh.setPosition(position)
  }

  def update(inputManager: InputManager, time: Double): Unit = {
    
    if (body == null) {
      return
    }
    
    var velocity = body.getLinearVelocity
    
    if (currentDirection == 0 && currentRotation > 0) {
      currentRotation -= RotationStep
    } else if (currentDirection == 1 && currentRotation < 180) {
      currentRotation += RotationStep
    }
    //Vertical rotation (on gravity switch)
    if(body.getWorld.getGravity.y > 0 && verticalRotation < 180){
      verticalRotation += RotationStep
    } else if(body.getWorld.getGravity.y < 0 && verticalRotation > 0){
      verticalRotation -= RotationStep
    }
    mesh.setTransform(Mat4.multiply(Mat4.rotate(verticalRotation, 1, 0, 0), Mat4.rotate(currentRotation, 0, 1, 0)))
    
    if(time - timeOfAnimation > Math.abs(0.7 / velocity.x) || time - timeOfAnimation > 0.2){
      timeOfAnimation = time
      if ((!isOnGround && body.getWorld.getGravity.y < 0) || (!isOnTop && body.getWorld.getGravity.y > 0)) {
        mesh.getMaterial.asInstanceOf[ColorMapMaterial].setColorMap(materialPlayerJump.getColorMap)
      } else {
        if(Math.abs(velocity.x) < 0.5) {
          mesh.getMaterial.asInstanceOf[ColorMapMaterial].setColorMap(materialPlayer.getColorMap)
        } else {
          mesh.getMaterial.asInstanceOf[ColorMapMaterial].setColorMap(walkingAnimation(stepAnimation).getColorMap)
          stepAnimation = (stepAnimation + 1) % walkingAnimation.length;
        } 
      }
    }
    
    mesh.setPosition(new Vec3(body.getPosition.x, body.getPosition.y, 0.5))
    
    if (inputManager.keyPressed(KeyEvent.VK_RIGHT)) {
      body.setLinearVelocity(velocity.add(new org.jbox2d.common.Vec2(Player.MoveVelocity, 0f)))
      currentDirection = 0
    }
    if (inputManager.keyPressed(KeyEvent.VK_LEFT)) {
      body.setLinearVelocity(velocity.add(new org.jbox2d.common.Vec2(-Player.MoveVelocity, 0f)))
      currentDirection = 1
    }
    if (inputManager.keyWasPressed(KeyEvent.VK_SPACE) && jumpCount > 0) {
      SoundManager.playSound(SoundManager.JumpSound)
      if (body.getWorld.getGravity.y < 0) {
        body.setLinearVelocity(new org.jbox2d.common.Vec2(velocity.x, Player.JumpVelocity))
      } else {
        body.setLinearVelocity(new org.jbox2d.common.Vec2(velocity.x, -Player.JumpVelocity))
      }
      jumpCount -= 1
    }

    velocity = body.getLinearVelocity
    if (Math.abs(velocity.x) > Player.MaxXVelocity) {
      if(velocity.x > 0) {
         body.setLinearVelocity(new org.jbox2d.common.Vec2(Player.MaxXVelocity, velocity.y))
      } else {
         body.setLinearVelocity(new org.jbox2d.common.Vec2(-Player.MaxXVelocity, velocity.y))
      }
    }
  }

  def beginContact(self: Fixture, other: Fixture, contact: Contact): Unit = {
    if (self == groundSensor) {
      if (body.getWorld.getGravity.y < 0) {
        jumpCount = 2
      }
      onGroundCount += 1
    } else if (self == topSensor) {
      if (body.getWorld.getGravity.y > 0) {
        jumpCount = 2
      }
      onTopCount += 1
    }
  }

  def endContact(self: Fixture, other: Fixture, contact: Contact): Unit = {
    if (self == groundSensor) {
      onGroundCount -= 1
    } else if (self == topSensor) {
      onTopCount -= 1
    }
  }

  def isOnGround(): Boolean = onGroundCount > 0
  def isOnTop(): Boolean = onTopCount > 0
}

object Player {
  val RotationStep = 10.0f
  val MaxXVelocity: Float = 10.0f
  val MoveVelocity: Float = 1.0f
  val JumpVelocity: Float = 11.0f
  val z = 0f;
  val e = .7f;
  val texCoords = Array( 0f, 0f, 1f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 1f )
  val vertices = Array( -e, -e, z, e, -e, z, e, e, z, -e, -e, z, e, e, z, -e, e, z )
  
 
  val g = DefaultGeometry.createVM(Primitive.TRIANGLES, vertices, texCoords);
  
  def createMesh(player: Player): IMesh = {
    val materialPlayer = new ColorMapMaterial(Frame.create(getClass.getResource("../assets/player.png")).getTexture())
    val mesh = new DefaultMesh(materialPlayer, g, Queue.TRANSPARENCY);
    mesh.setPosition(player.position toVec3 0.5f)
    mesh
  }
  
  def createPlayerPolygon(hw: Float, hh: Float, cornerSize: Float): Array[org.jbox2d.common.Vec2] = {
    Array(
      new org.jbox2d.common.Vec2(-hw + cornerSize, hh),
      new org.jbox2d.common.Vec2(-hw, hh - cornerSize),
      new org.jbox2d.common.Vec2(-hw + cornerSize, -hh),
      new org.jbox2d.common.Vec2(-hw, -hh + cornerSize),
      new org.jbox2d.common.Vec2(hw - cornerSize, -hh),
      new org.jbox2d.common.Vec2(hw, -hh + cornerSize),
      new org.jbox2d.common.Vec2(hw, hh - cornerSize),
      new org.jbox2d.common.Vec2(hw - cornerSize, hh))
  }
}