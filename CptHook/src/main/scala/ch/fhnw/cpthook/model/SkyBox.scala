package ch.fhnw.cpthook.model

import ch.fhnw.ether.image.Frame
import ch.fhnw.ether.scene.mesh.DefaultMesh
import ch.fhnw.ether.scene.mesh.IMesh
import ch.fhnw.ether.scene.mesh.IMesh.Queue
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.Primitive
import ch.fhnw.ether.scene.mesh.material.ColorMapMaterial

class SkyBox {
  val z = 0f;
  val e = 90f;
  val texCoords = Array( 0f, 0f, 1f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 1f )
  val vertices = Array( -e, -e, z, e, -e, z, e, e, z, -e, -e, z, e, e, z, -e, e, z )
  
  val materialPlayer = new ColorMapMaterial(Frame.create(getClass.getResource("../assets/skybox.png")).getTexture())
  val g = DefaultGeometry.createVM(Primitive.TRIANGLES, vertices, texCoords);
  
  def createMesh(): IMesh = {
    val mesh = new DefaultMesh(materialPlayer, g, Queue.DEPTH);
    mesh
  }
}