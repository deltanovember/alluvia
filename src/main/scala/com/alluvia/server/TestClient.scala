package com.alluvia.server

import akka.actor.Actor
import com.alluvialtrading.fix.Order

object TestClient {


  def run = {
    val actor = Actor.remote.actorFor("order-service", "localhost", 2552)
    val order = new Order()
    order.setSymbol("BHP")
    order.setLimit("95.42")
    order.setMessage("SEND")

    val result = actor !! order
  }

  def main(args: Array[String]) = run
}