package ch.fhnw.cpthook.model

import org.jbox2d.dynamics.World
import ch.fhnw.ether.scene.mesh.IMesh
import ch.fhnw.ether.scene.mesh.DefaultMesh
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.Primitive
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry
import ch.fhnw.ether.scene.mesh.IMesh.Queue
import ch.fhnw.util.math.Vec3
import ch.fhnw.cpthook.InputManager
import ch.fhnw.cpthook.tools.GameContactListener
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.Fixture
import org.jbox2d.callbacks.ContactListener
import ch.fhnw.cpthook.tools.ContactUpdates
import org.jbox2d.dynamics.contacts.Contact
import org.jbox2d.dynamics.Body
import ch.fhnw.ether.scene.mesh.material.ColorMapMaterial
import ch.fhnw.util.math.Mat4

class Monster(var position: Position) extends Entity
  with EntitiyUpdatable
  with EntityActivatable
  with ContactUpdates
  with IGameStateChanger {
  
  var mesh: IMesh = null
  var body: Body = null
  var leftSensor: Fixture = null
  var rightSensor: Fixture = null
  
  var velocity: Float = -Monster.Speed

  // animation
  var animationStep = 0
  var lastTimeOfAnimation: Double = 0.0
  var currentRotation: Double = 0.0

  private var controller: IGameStateController = null;
  def init(controller: IGameStateController): Unit = { this.controller = controller }

  def toMesh(): IMesh = Monster.toMesh(this)
  def linkBox2D(world: World): Unit = Monster.linkBox2d(this, world)
  
  def activate(gameContactListener: GameContactListener): Unit = {
    currentRotation = 0
    animationStep = 0
    lastTimeOfAnimation = 0
    velocity = -Monster.Speed
    gameContactListener.register(this, leftSensor)
    gameContactListener.register(this, rightSensor)
    
  }
  
  def deactivate(): Unit = {
    resetMeshPosition()
  }
  
  def resetMeshPosition() {
    mesh.setPosition(position add new Vec3(Monster.RealDimensions._1 / 2f, -Monster.RealDimensions._2 / 2f, 0))
    mesh.setTransform(Mat4.multiply(Mat4.rotate(-currentRotation.floatValue(), new Vec3(0, 1, 0)), mesh.getTransform))
  }
  
  def update(inputManager: InputManager, time: Double): Unit = {
    
    val delta = time - lastTimeOfAnimation
    
    // animation
    if (delta > Monster.AnimationStepTime) {
      lastTimeOfAnimation = time
      animationStep = (animationStep + 1) % Monster.AnimationFrames.length
      mesh.getMaterial().asInstanceOf[ColorMapMaterial].setColorMap(Monster.AnimationFrames(animationStep))
    }
    
    // paper mario effect
    if (velocity == -Monster.Speed && currentRotation > 0) {
      mesh.setTransform(Mat4.multiply(Mat4.rotate(Monster.RotationStep, new Vec3(0, 1, 0)), mesh.getTransform))
      currentRotation -= Monster.RotationStep
    } else if (velocity == Monster.Speed && currentRotation < 180) {
      mesh.setTransform(Mat4.multiply(Mat4.rotate(Monster.RotationStep, new Vec3(0, 1, 0)), mesh.getTransform))
      currentRotation += Monster.RotationStep
    }  
    
    val v = body.getLinearVelocity
    body.setLinearVelocity(new org.jbox2d.common.Vec2(velocity, v.y))
    val newPosition = new Vec3(body.getPosition.x, body.getPosition.y, 0f)
    mesh.setPosition(newPosition)
  }
  
  def beginContact(self: Fixture, other: Fixture,contact: Contact): Unit = {
    if (self == leftSensor && other.getBody.getUserData.isInstanceOf[Block]) {
      velocity = Monster.Speed
    } else if (self == rightSensor && other.getBody.getUserData.isInstanceOf[Block] ) {
      velocity = -Monster.Speed
    } else {}

    if(other.getBody.getUserData.isInstanceOf[Player] && controller != null){
      controller.gameOver
    }
  }
  
  def endContact(self: Fixture, other: Fixture,contact: Contact): Unit = {
    
  }
  
}

object Monster {
  
  val Speed = 2f
  val Box2DDimensions = (0.9f, 0.9f)
  val RealDimensions = (1.0f, 1.0f)
  val AnimationFrames = (1 to 11).map { n => Entity.loadTexture(s"../assets/monster_step$n.png") }.toArray
  val AnimationStepTime = 0.3f
  val RotationStep = 10
  
  val Vertices = Entity.twoDimensionalPlane(Box2DDimensions._1, Box2DDimensions._2, 0f)
  val Geometry = DefaultGeometry.createVM(Primitive.TRIANGLES, Vertices, Entity.defaultTextureCoordinates)
  
  def toMesh(monster: Monster): IMesh = {
    monster.mesh = new DefaultMesh(new ColorMapMaterial(AnimationFrames(0)), Geometry, Queue.TRANSPARENCY);
    monster.resetMeshPosition()
    monster.mesh
  }
  
  def linkBox2d(monster: Monster, world: World): Unit = {
    val bodyDef = new BodyDef
    bodyDef.position.set(monster.position.x + RealDimensions._1 / 2.0f, monster.position.y - RealDimensions._2 / 2.0f)
    bodyDef.`type` = BodyType.DYNAMIC
    bodyDef.fixedRotation = true
    
    val shape: PolygonShape = new PolygonShape
    shape.set(Player.createPlayerPolygon(Box2DDimensions._1  / 2f, Box2DDimensions._2 / 2f, 0.1f), 8)
    val fixtureDef: FixtureDef = new FixtureDef
    fixtureDef.shape = shape
    fixtureDef.friction = 0.1f;        
    fixtureDef.restitution = 0.1f;
    fixtureDef.density = 1;
        
    val leftSensor: PolygonShape = new PolygonShape
    leftSensor.setAsBox(0.1f, Box2DDimensions._2 / 2f - 0.1f, new org.jbox2d.common.Vec2(-Box2DDimensions._1 / 2f, 0f), 0f)
    val leftSensorFixtureDef: FixtureDef = new FixtureDef
    leftSensorFixtureDef.shape = leftSensor
    leftSensorFixtureDef.isSensor = true
    
    val rightSensor: PolygonShape = new PolygonShape
    rightSensor.setAsBox(0.1f, Box2DDimensions._2 / 2f - 0.1f, new org.jbox2d.common.Vec2(Box2DDimensions._1 / 2f, 0f), 0f)
    val rightSensorFixtureDef: FixtureDef = new FixtureDef
    rightSensorFixtureDef.shape = rightSensor
    rightSensorFixtureDef.isSensor = true
    
    monster.body = world.createBody(bodyDef)
    monster.body.createFixture(fixtureDef)
    monster.leftSensor = monster.body.createFixture(leftSensorFixtureDef)
    monster.rightSensor = monster.body.createFixture(rightSensorFixtureDef)
    monster.body.setUserData(monster)
  }
}