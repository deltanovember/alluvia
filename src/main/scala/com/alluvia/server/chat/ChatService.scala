package com.alluvia.server.chat

import akka.actor.Actor.remote

class ChatService extends ChatServer with SessionManagement with ChatManagement with MemoryChatStorageFactory {

  override def preStart() = {
    remote.start("localhost", 2552)
    remote.register("chat:service", self)
  }

}