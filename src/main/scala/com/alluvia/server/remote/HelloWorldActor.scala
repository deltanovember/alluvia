package com.alluvia.server.remote

import akka.actor.Actor
import Actor._
import akka.event.EventHandler
import collection.mutable.ListBuffer

class HelloWorldActor extends Actor {

  val clients = new ListBuffer[akka.actor.UntypedChannel]

  def receive = {
    case "Hello" =>
      clients.append(self.channel)
      clients.head ! "Pong1"
      clients.head ! "SecondPong1"
      println("received Hello, storing client")
    case "Ping" =>
      println("received ping")
      self.channel ! "Reply client 2"
      clients.head ! ("Pong2")
  }
}

object ServerInitiatedRemoteActorServer {

  def run() {
    remote.start("localhost", 2552)
    remote.register("hello-service", actorOf[HelloWorldActor])
  }

  def main(args: Array[String]) {
    run()
  }
}
