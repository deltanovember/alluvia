package com.alluvia.server.sandbox

import akka.actor.Actor.spawn

object Spawn extends App {

  spawn {
    for (i <- 0 to 100) println("blah")
  }
  println("yes")
}