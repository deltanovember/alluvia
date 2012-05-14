package com.alluvia.server.remote

import akka.actor.Actor
import Actor._

class HelloWorldActorClient extends Actor {

  override def preStart {
    println("Client executing")
    val actor = remote.actorFor("hello-service", "localhost", 2552)
    actor ! "Hello"
  }


  def receive = {
    case "Pong1" => "Received P1"
    case _ => "Unknown"
  }

}