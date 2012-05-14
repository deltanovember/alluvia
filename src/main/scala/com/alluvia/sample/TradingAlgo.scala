package com.alluvia.sample

import com.alluvia.types.market.Start
import akka.actor.Actor
import com.alluvialtrading.fix.{OrderSide, Order}
import com.alluvia.algo.{TypeConverter, EventAlgo}
import com.alluvia.markets.ASX
import java.util.Date
import com.alluvia.algo.datasource.{Historical, IressReplay}

trait TradingAlgo extends EventAlgo {

  val actor = Actor.remote.actorFor("order-service", "localhost", 2552)

  override def onStart(s: Start) {

    val order = generateOrder(OrderSide.BUY, "BHP.AX", 20.0, 100)
   (actor ? order).as[Order]
  }

}

object RunTradingAlgo extends TypeConverter {

  def main(args: Array[String]) {
        new IressReplay with ASX with TradingAlgo {
      val startDate: Date = "2011-11-16"
      val endDate: Date = "2011-11-16"
    } run
  }

}