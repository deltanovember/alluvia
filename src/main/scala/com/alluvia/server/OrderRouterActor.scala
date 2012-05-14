package com.alluvia.server

import akka.actor.Actor
import org.slf4j.{LoggerFactory, Logger}
import java.io.{FileInputStream, InputStream}
import com.alluvia.fix.{FixApplication, FixProcessor}
import quickfix._
import com.alluvialtrading.fix.{OrderTIF, OrderType, OrderSide, Order}
import com.alluvia.patterns.Observer
import com.alluvia.types.ObservedEventPump

abstract class OrderRouterActor extends Actor with Observer[Any] {


  def receive = {
    //application.send(order)
    //case order: Order =>  processOrder(order)
    case _ => println("Unknown message")
  }


}