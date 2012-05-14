package com.alluvia.server.chat

import akka.actor.{ActorRef, Actor}

import akka.event.EventHandler
import collection.mutable.HashMap

trait ChatManagement { this: Actor =>
  val sessions: HashMap[String, ActorRef] // needs someone to provide the Session map

  protected def chatManagement: Receive = {
    case msg @ ChatMessage(from, _) => getSession(from).foreach(_ ! msg)
    case msg @ GetChatLog(from) =>     getSession(from).foreach(_ forward msg)
  }

  private def getSession(from: String) : Option[ActorRef] = {
    if (sessions.contains(from))
      Some(sessions(from))
    else {
      EventHandler.info(this, "Session expired for %s".format(from))
      None
    }
  }
}