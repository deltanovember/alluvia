package com.alluvia.algo.datasource

/**
 * Retrieve historical events from database
 */

import com.alluvia.types.ObservedEventPump
import com.alluvia.algo.EventAlgo
import collection.mutable.HashMap
import com.alluvialtrading.fix.{OrderType, Order, OrderSide}

trait Historical extends EventAlgo {

  val startDate: java.util.Date
  val endDate: java.util.Date

  override def printToBrowser = false
  override def performExit = true
  override def delayBetweenTrades = 0.seconds

   override def generateOrder(side: OrderSide, symbol: String, limit: Double, quantity: Int): Order = {
    val order = super.generateOrder(side, symbol, limit, quantity)
     val security = order.getSymbol.split("\\." + getSecurityExchange)(0)
     // Hack avgpx for profit tracking
     if (strictPricing) {
       order.setAvgPx(latestPrices(security))
     }
    order
  }

  override def maxOrders = 15
  //
  override def run {
    val eventPump = new ObservedEventPump
    eventPump.addObserver(this)

    val historicalPump = new ObservedEventPump
    historicalPump.addObserver(this)
    if (benchmarkDays > 0) {
      println("benchmarking")
      connector.getAllEvents(getMarketName, historicalPump,
        lib.combineDateTime(getTradingDate(startDate, -benchmarkDays), "00:00:00.001"), lib.combineDateTime(startDate, "00:00:00.001"), true)
    }
    connector.getAllEvents(getMarketName, eventPump,
      lib.combineDateTime(startDate, "00:00:00.001"), lib.combineDateTime(endDate, "23:59:59.999"))
  }

}