package com.alluvia.server.remote

import akka.actor.Actor

object Client1 {

  def run() {
    val myActor = Actor.actorOf[HelloWorldActorClient]
    myActor.start()
  }

  def main(args: Array[String]) {
    run()
  }
}