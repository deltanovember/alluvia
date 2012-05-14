package com.alluvia.server.chat

import akka.actor.{ActorRef, Actor}
import akka.event.EventHandler
import akka.config.Supervision.OneForOneStrategy

trait ChatServer extends Actor {
  self.faultHandler = OneForOneStrategy(List(classOf[Exception]), 5, 5000)
  val storage: ActorRef

  EventHandler.info(this, "chat server starting")
  def receive: Receive = sessionManagement orElse chatManagement

  protected def chatManagement: Receive
  protected def sessionManagement: Receive
  protected def shutdownSessions(): Unit

  override def postStop() = {
    EventHandler.info(this, "Chat server is shutting down")
    shutdownSessions()
    self.unlink(storage)
    storage.stop()
  }


}