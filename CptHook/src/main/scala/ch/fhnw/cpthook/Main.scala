package ch.fhnw.cpthook

import ch.fhnw.ether.controller.DefaultController
import ch.fhnw.ether.controller.event.IEventScheduler.IAction
import ch.fhnw.ether.view.gl.DefaultView
import ch.fhnw.ether.view.IView
import ch.fhnw.ether.scene.DefaultScene
import ch.fhnw.ether.scene.IScene
import ch.fhnw.ether.controller.IController
import ch.fhnw.ether.scene.light.DirectionalLight
import ch.fhnw.cpthook.json.JsonSerializer
import ch.fhnw.ether.view.IView._
import ch.fhnw.cpthook.viewmodel.ILevelViewModel
import ch.fhnw.cpthook.viewmodel.LevelViewModel
import ch.fhnw.util.math.Vec3
import ch.fhnw.ether.scene.light.PointLight
import ch.fhnw.cpthook.tools.EditorTool

trait Context {
  def controller: IController
  def scene: IScene
  var view: IView
}

object Main extends App with Context {
  override val controller = new CptHookController
  override val scene: IScene = new DefaultScene(controller)
  override var view: IView = null
  controller.run(new ControllerAction(Main, Defaults))
}

class ControllerAction(val context: Context, val config: Configuration) extends IAction {
  import context._
  def run(time: Double): Unit = {
    //Create default viewport with light
    val viewConfig = new Config(ViewType.INTERACTIVE_VIEW, 0, ViewFlag.SMOOTH_LINES)
    view = new DefaultView(
      controller, config.windowPosition._1, config.windowPosition._2,
      config.windowSize._1, config.windowSize._2, viewConfig, config.windowTitle)   
    controller.setScene(scene)
    scene.add3DObject(new DirectionalLight(config.lightDirection, config.ambient, config.lightColor))
    
    val camera = controller.getCamera(view)
    camera.setUp(config.cameraUp)
    camera.setPosition(config.cameraPosition)
    
    //Load the example level
    val level = JsonSerializer.readLevel("save.json")
    val viewModel: ILevelViewModel = new LevelViewModel(level, scene)
    
    //Add editor tool
    val editorTool = new EditorTool(controller, camera, viewModel)
    controller.setCurrentTool(editorTool)
  }
}