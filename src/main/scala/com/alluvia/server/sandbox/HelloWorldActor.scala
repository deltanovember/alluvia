package com.alluvia.server.sandbox

import akka.actor.Actor._
import akka.actor. {ActorRegistry, Actor}

class HelloWorldActor extends Actor {
  def receive = {
    case "Hello" =>  println("World")
  }
}

object ServerManagedRemoteActorServer {

  def run = {
    Actor.remote.start("localhost", 2552)
    Actor.remote.register("hello-service", actorOf[HelloWorldActor])
  }

  def main(args: Array[String]) = run
}

object ServerManagedRemoteActorClient {

  def run = {
    val actor = Actor.remote.actorFor("hello-service", "localhost", 2552)
    val result = actor !! "Hello"
  }

  def main(args: Array[String]) = run
}