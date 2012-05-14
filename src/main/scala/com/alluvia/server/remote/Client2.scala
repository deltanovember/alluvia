package com.alluvia.server.remote

import akka.event.EventHandler
import akka.actor.Actor
import Actor._

object Client2 {

  def run() {
    val actor = remote.actorFor("hello-service", "localhost", 2552)
    val result = (actor ? "Ping").as[AnyRef]
    EventHandler.info("Result from Remote Actor: %s", result)
  }

  def main(args: Array[String]) { run() }
}