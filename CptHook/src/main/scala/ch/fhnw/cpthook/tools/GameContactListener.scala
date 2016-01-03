package ch.fhnw.cpthook.tools

import org.jbox2d.callbacks.ContactListener
import org.jbox2d.dynamics.contacts.Contact
import org.jbox2d.callbacks.ContactImpulse
import org.jbox2d.collision.Manifold
import org.jbox2d.dynamics.Fixture

class GameContactListener extends ContactListener {
  
  private var listeners: Map[Fixture, ContactUpdates] = Map()
  
  def register(listener: ContactUpdates, listenTo: Fixture): Unit = {
    listeners += (listenTo -> listener)
  }
  
  def beginContact(contact: Contact): Unit = {
    var optionalListener = listeners.get(contact.getFixtureA)
    if(optionalListener.isDefined) {
      optionalListener.get.beginContact(contact.getFixtureB, contact)
    }
    optionalListener = listeners.get(contact.getFixtureB)
    if(optionalListener.isDefined) {
      optionalListener.get.beginContact(contact.getFixtureA, contact)
    }
  }
  
  def endContact(contact: Contact): Unit = {
    var optionalListener = listeners.get(contact.getFixtureA)
    if(optionalListener.isDefined) {
      optionalListener.get.endContact(contact.getFixtureB, contact)
    }
    optionalListener = listeners.get(contact.getFixtureB)
    if(optionalListener.isDefined) {
      optionalListener.get.endContact(contact.getFixtureA, contact)
    }
  }
  
  def postSolve(contact: Contact, contactImpulse: ContactImpulse): Unit = {}
  
  def preSolve(contact: Contact, manifold: Manifold): Unit = {}
  
}

trait ContactUpdates {
  def beginContact(otherFixture: Fixture, contact: Contact): Unit
  def endContact(otherFixture: Fixture, contact: Contact): Unit
}