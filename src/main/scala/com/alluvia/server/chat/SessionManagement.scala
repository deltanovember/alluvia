package com.alluvia.server.chat

import akka.actor.{ActorRef, Actor}
import akka.actor.Actor.actorOf
import akka.event.EventHandler
import collection.mutable.HashMap

trait SessionManagement { this: Actor =>

  val storage: ActorRef // needs someone to provide the ChatStorage
  val sessions = new HashMap[String, ActorRef]

  protected def sessionManagement: Receive = {
    case Login(username) =>
      EventHandler.info(this, "User [%s] has logged in".format(username))
      val session = actorOf(new Session(username, storage))
      session.start()
      sessions += (username -> session)

    case Logout(username) =>
      EventHandler.info(this, "User [%s] has logged out".format(username))
      val session = sessions(username)
      session.stop()
      sessions -= username
  }

  protected def shutdownSessions =
    sessions.foreach { case (_, session) => session.stop() }
}