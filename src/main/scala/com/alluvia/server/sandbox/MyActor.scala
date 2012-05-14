package com.alluvia.server.sandbox

import akka.actor.Actor
import akka.event.EventHandler

class MyActor(msg: String) extends Actor {

  def receive = {
    case "test" => EventHandler.info(this, "received test")
    case "Hello" => println("received HI", msg)
    case _ => EventHandler.info(this, "received unknown message")
  }
}