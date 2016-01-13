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
 * Block which sets the players checkpoint when touched.
 * If the player dies after that, he respawns at that checkpoint.
 */
class CheckpointBlock(position: Position, size: Size) extends GameStateBlock(position, size, Block.CheckpointTexture) with EntityActivatable with ContactUpdates {
  def getFriction = 0.2f
  def getRestitution = 0.1f

  //Store body and mesh, when available
  var body: Body = null
  var mesh: IMesh = null
  override def toMesh = { mesh = super.toMesh; mesh }
  override def linkBox2D(world: World): Unit = { body = Block.createDefaultBox2D(world, this) }
  def deactivate(): Unit = {
    body = null
    disable
  }
  def activate(gameContactListener: GameContactListener): Unit = {
    if(body != null) { gameContactListener.register(this, body.getFixtureList) }
  }

  //Change gravity when collision is detected
  def beginContact(self: Fixture, other: Fixture, contact: Contact): Unit = other.getBody.getUserData match {
    case player: Player if getController != null =>
      getController.setCheckpoint(this)
      if(mesh.getMaterial.isInstanceOf[ColorMapMaterial]){
        mesh.getMaterial.asInstanceOf[ColorMapMaterial].setColorMap(Block.CheckpointActiveTexture)
      }
    case _ =>
  }
  def endContact(self: Fixture, other: Fixture, contact: Contact): Unit = {}
  /**
   * Disables the checkpoint: The player will no longer spawn here.
   */
  def disable: Unit = {
    if(mesh.getMaterial.isInstanceOf[ColorMapMaterial]){
      mesh.getMaterial.asInstanceOf[ColorMapMaterial].setColorMap(Block.CheckpointTexture)
    }
  }
}