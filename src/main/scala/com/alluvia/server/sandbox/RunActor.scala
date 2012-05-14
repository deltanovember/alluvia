package com.alluvia.server.sandbox

import akka.actor.Actor


object RunActor extends App {

 // val myActor = Actor.actorOf[MyActor]
  val myActor = Actor.actorOf(new MyActor("mymessage")).start()
  myActor.start()
  myActor ? "Hello"
}