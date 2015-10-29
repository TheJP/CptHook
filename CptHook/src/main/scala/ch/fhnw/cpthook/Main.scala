package ch.fhnw.cpthook

import ch.fhnw.ether.controller.DefaultController
import ch.fhnw.ether.controller.event.IScheduler.IAction
import ch.fhnw.ether.view.gl.DefaultView
import ch.fhnw.ether.view.IView
import ch.fhnw.ether.scene.DefaultScene
import ch.fhnw.ether.scene.IScene

object Main extends App {
  val controller = new DefaultController
  val scene: IScene = new DefaultScene(controller)
  controller.run(ControllerAction)
}

object ControllerAction extends IAction {
  def run(time: Double): Unit = {
    new DefaultView(Main.controller, 100, 100, 500, 500, IView.INTERACTIVE_VIEW, "CptHook")
    Main.controller.setScene(Main.scene)
  }
}