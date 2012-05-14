package com.alluvia.server.chat

import akka.actor.Actor.actorOf

/**
 * Test runner starting ChatService.
 */
object ServerRunner {

  def main(args: Array[String]) { ServerRunner.run() }

  def run() {
    actorOf[ChatService]
  }
}
