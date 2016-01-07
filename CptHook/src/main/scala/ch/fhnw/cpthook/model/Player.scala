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


class Player(var position: Position) extends ContactUpdates {
  
  var body: Body = null
  var jumpCount: Integer = 0
  var onGround = false
  var stepAnimation: Integer = 0
  val mesh: IMesh = Player.createMesh(this)
  val materialPlayerStep = new ColorMapMaterial(Frame.create(getClass.getResource("../assets/player_step.png")).getTexture())
  val materialPlayerStep2 = new ColorMapMaterial(Frame.create(getClass.getResource("../assets/player_step2.png")).getTexture())
  val materialPlayer = new ColorMapMaterial(Frame.create(getClass.getResource("../assets/player.png")).getTexture())
  val materialPlayerJump = new ColorMapMaterial(Frame.create(getClass.getResource("../assets/player_jump.png")).getTexture())
  var timeOfAnimation: Double = 0.0
  var currentRotation: Double = 0.0
  var currentDirection: Integer = 0
  
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
    groundSensorShape.setAsBox(0.2f, 0.1f, new org.jbox2d.common.Vec2(0f, -0.7f), 0f)
    val groundSensorFixtureDef: FixtureDef = new FixtureDef
    groundSensorFixtureDef.shape = groundSensorShape
    groundSensorFixtureDef.isSensor = true
    
    body = world.createBody(bodyDef)
    body.createFixture(fixtureDef)
    body.createFixture(groundSensorFixtureDef)
    body.setUserData(this)
  }
  
  def unlinkBox2D(world: World): Unit = {
    body = null
  }
  
  def update(inputManager: InputManager, time: Double): Unit = {
    
    if (body == null) {
      return
    }
    
   
    
    var velocity = body.getLinearVelocity
    
    if (currentDirection == 0 && currentRotation > 0) {
      mesh.setTransform(Mat4.multiply(Mat4.rotate(-10, new Vec3(0, 1, 0)), mesh.getTransform))
      currentRotation -= 10
    } else if (currentDirection == 1 && currentRotation < 180) {
      mesh.setTransform(Mat4.multiply(Mat4.rotate(10, new Vec3(0, 1, 0)), mesh.getTransform))
      currentRotation += 10
    }
    
    if(time - timeOfAnimation > Math.abs(0.3/velocity.x) || time - timeOfAnimation > 0.5){
      timeOfAnimation = time
      if(!onGround) {
        if (velocity.y > 0.1) {
          mesh.getMaterial().asInstanceOf[ColorMapMaterial].setColorMap(materialPlayerJump.getColorMap())
        } else {
          mesh.getMaterial().asInstanceOf[ColorMapMaterial].setColorMap(materialPlayer.getColorMap())
        }
      } else {
        if(stepAnimation == 0){
          mesh.getMaterial().asInstanceOf[ColorMapMaterial].setColorMap(materialPlayer.getColorMap())
          stepAnimation = 1;
        } else if(stepAnimation == 1){
          mesh.getMaterial().asInstanceOf[ColorMapMaterial].setColorMap(materialPlayerStep.getColorMap())
          stepAnimation = 2;
        } else {
          mesh.getMaterial().asInstanceOf[ColorMapMaterial].setColorMap(materialPlayerStep2.getColorMap())
          stepAnimation = 0;
        }
      }
    }
    
    mesh.setPosition(new Vec3(body.getPosition.x, body.getPosition.y,0.5))
    
    if (inputManager.keyPressed(KeyEvent.VK_RIGHT)) {
      body.setLinearVelocity(velocity.add(new org.jbox2d.common.Vec2(Player.MoveVelocity, 0f)))
      currentDirection = 0
    }
    if (inputManager.keyPressed(KeyEvent.VK_LEFT)) {
      body.setLinearVelocity(velocity.add(new org.jbox2d.common.Vec2(-Player.MoveVelocity, 0f)))
      currentDirection = 1
    }
    if (inputManager.keyWasPressed(KeyEvent.VK_SPACE) && jumpCount > 0) {
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
  
  def beginContact(otherFixture: Fixture,contact: Contact): Unit = {
    jumpCount = 2
    onGround = true
  }
  
  def endContact(otherFixture: org.jbox2d.dynamics.Fixture,contact: org.jbox2d.dynamics.contacts.Contact): Unit = {
    onGround = false
  }
}

object Player {
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