package ch.fhnw.cpthook.model

import org.jbox2d.dynamics.World
import ch.fhnw.ether.scene.mesh.IMesh
import ch.fhnw.ether.scene.mesh.DefaultMesh
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.Primitive
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry
import ch.fhnw.ether.scene.mesh.IMesh.Queue
import ch.fhnw.util.math.Vec3

class Monster(var position: Position) extends Entity with EntitiyUpdatable with EntityActivatable {
  
  var animationStep = 0
  
  // Members declared in ch.fhnw.cpthook.model.Entity
  def toMesh(): ch.fhnw.ether.scene.mesh.IMesh = Monster.toMesh(this)
  def linkBox2D(world: World): Unit = Monster.linkBox2d(this, world)
  
    // Members declared in ch.fhnw.cpthook.model.EntityActivatable
  def activate(gameContactListener: ch.fhnw.cpthook.tools.GameContactListener): Unit = {
    
  }
  def deactivate(): Unit = {
    
  }
  
  def update(inputManager: ch.fhnw.cpthook.InputManager,time: Double): Unit = {
    
  }
  
}

object Monster {
  
  val width = 1f
  val height = 1f

  val animationFrames = (1 to 11).map { n => Entity.loadMaterial(s"../assets/monster_step$n.png") }.toArray
  
  val vertices = Entity.twoDimensionalPlane(width, height, 0f)
  val geometry = DefaultGeometry.createVM(Primitive.TRIANGLES, vertices, Entity.defaultTextureCoordinates)
  
  def toMesh(monster: Monster): IMesh = {
    val mesh = new DefaultMesh(animationFrames(0), geometry, Queue.TRANSPARENCY);
    mesh.setPosition(monster.position.add(new Vec3(width / 2f, height / 2f, -0.5f)))
    mesh
  }
  
  def linkBox2d(monster: Monster, world: World): Unit = {
    
  }
}