package ch.fhnw.cpthook.model

import ch.fhnw.cpthook.tools.ContactUpdates
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.World
import ch.fhnw.cpthook.tools.GameContactListener
import org.jbox2d.dynamics.contacts.Contact
import org.jbox2d.dynamics.Fixture
import ch.fhnw.ether.scene.mesh.IMesh
import ch.fhnw.ether.scene.mesh.material.ColorMapMaterial

/**
 * Block which switches gravity, when it's touched for the first time.
 */
class GravityBlock(position: Position, size: Size) extends GameStateBlock(position, size, Block.GravityTexture) with EntityActivatable with ContactUpdates {
  def getFriction = 0.2f
  def getRestitution = 0.1f
  var used = false

  //Store body and mesh, when available
  var body: Body = null
  var mesh: IMesh = null
  override def toMesh = { mesh = super.toMesh; mesh }
  override def linkBox2D(world: World): Unit = { body = Block.createDefaultBox2D(world, this) }
  def deactivate(): Unit = {
    body = null
    if(mesh.getMaterial.isInstanceOf[ColorMapMaterial]){
      mesh.getMaterial.asInstanceOf[ColorMapMaterial].setColorMap(Block.GravityTexture)
    }
  }
  def activate(gameContactListener: GameContactListener): Unit = {
    used = false
    if(body != null) { gameContactListener.register(this, body.getFixtureList) }
  }

  //Change gravity when collision is detected
  def beginContact(self: Fixture, other: Fixture, contact: Contact): Unit = other.getBody.getUserData match {
    case player: Player if !used && getController != null =>
      used = true
      getController.switchGravity
      if(mesh.getMaterial.isInstanceOf[ColorMapMaterial]){
        mesh.getMaterial.asInstanceOf[ColorMapMaterial].setColorMap(Block.GravityUsedTexture)
      }
    case _ =>
  }
  def endContact(self: Fixture, other: Fixture, contact: Contact): Unit = {}
}