package com.alluvia.server.chat

import akka.actor.Actor

trait MemoryChatStorageFactory { this: Actor =>
  val storage = this.self.spawnLink[MemoryChatStorage]

}