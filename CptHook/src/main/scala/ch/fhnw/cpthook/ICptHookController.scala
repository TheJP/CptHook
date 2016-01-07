package ch.fhnw.cpthook

import ch.fhnw.ether.controller.IController

trait ICptHookController extends IController {
  def inputManager: InputManager
}