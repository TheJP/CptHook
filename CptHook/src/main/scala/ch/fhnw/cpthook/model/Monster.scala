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

class Monster(var position: Position) extends Entity with EntitiyUpdatable with EntityActivatable with ContactUpdates {
  
  var animationStep = 0
  var mesh: IMesh = null
  var body: Body = null
  var leftSensor: Fixture = null
  var rightSensor: Fixture = null
  var velocity: Float = -Monster.Speed
  
  def toMesh(): IMesh = Monster.toMesh(this)
  def linkBox2D(world: World): Unit = Monster.linkBox2d(this, world)
  
  def activate(gameContactListener: GameContactListener): Unit = {
    animationStep = 0
    velocity = -Monster.Speed
    gameContactListener.register(this, leftSensor)
    gameContactListener.register(this, rightSensor)
  }
  
  def deactivate(): Unit = {
    mesh.setPosition(position.add(new Vec3(Monster.Width / 2f, Monster.Height / 2f, -0.5f)))
  }
  
  def update(inputManager: InputManager,time: Double): Unit = {
    val v = body.getLinearVelocity
    body.setLinearVelocity(new org.jbox2d.common.Vec2(velocity, v.y))
    val newPosition = new Vec3(body.getPosition.x, body.getPosition.y, -0.5f)
    mesh.setPosition(newPosition)
  }
  
  def beginContact(self: Fixture, other: Fixture,contact: Contact): Unit = {
    if (self == leftSensor && other.getUserData.isInstanceOf[Block]) {
      println("hit left")
      velocity = Monster.Speed
    } else if (self == rightSensor&& other.getUserData.isInstanceOf[Block] ) {
      println("hit right")
      velocity = -Monster.Speed
    } else {}
  }
  
  def endContact(self: Fixture, other: Fixture,contact: Contact): Unit = {
    
  }
  
}

object Monster {
  

  val Speed = 1f
  val Width = 1f
  val Height = 1f
  val AnimationFrames = (1 to 11).map { n => Entity.loadMaterial(s"../assets/monster_step$n.png") }.toArray
  
  val Vertices = Entity.twoDimensionalPlane(Width, Height, 0f)
  val Geometry = DefaultGeometry.createVM(Primitive.TRIANGLES, Vertices, Entity.defaultTextureCoordinates)
  
  def toMesh(monster: Monster): IMesh = {
    monster.mesh = new DefaultMesh(AnimationFrames(0), Geometry, Queue.TRANSPARENCY);
    monster.mesh.setPosition(monster.position.add(new Vec3(Width / 2f, Height / 2f, -0.5f)))
    monster.mesh
  }
  
  def linkBox2d(monster: Monster, world: World): Unit = {
    val bodyDef = new BodyDef
    bodyDef.position.set(monster.position.x, monster.position.y)
    bodyDef.`type` = BodyType.DYNAMIC
    bodyDef.fixedRotation = true
    
    val shape: PolygonShape = new PolygonShape
    shape.set(Player.createPlayerPolygon(Width / 2f, Height / 2f, 0.1f), 8)
    val fixtureDef: FixtureDef = new FixtureDef
    fixtureDef.shape = shape
    fixtureDef.friction = 0.1f;        
    fixtureDef.restitution = 0.1f;
    fixtureDef.density = 1;
        
    val leftSensor: PolygonShape = new PolygonShape
    leftSensor.setAsBox(0.1f, Width - 0.1f, new org.jbox2d.common.Vec2(-Width / 2f, 0f), 0f)
    val leftSensorFixtureDef: FixtureDef = new FixtureDef
    leftSensorFixtureDef.shape = leftSensor
    leftSensorFixtureDef.isSensor = true
    
    val rightSensor: PolygonShape = new PolygonShape
    rightSensor.setAsBox(0.1f, Width - 0.1f, new org.jbox2d.common.Vec2(Width / 2f, 0f), 0f)
    val rightSensorFixtureDef: FixtureDef = new FixtureDef
    rightSensorFixtureDef.shape = leftSensor
    rightSensorFixtureDef.isSensor = true
    
    monster.body = world.createBody(bodyDef)
    monster.body.createFixture(fixtureDef)
    monster.leftSensor = monster.body.createFixture(leftSensorFixtureDef)
    monster.rightSensor = monster.body.createFixture(rightSensorFixtureDef)
    monster.body.setUserData(this)
  }
}