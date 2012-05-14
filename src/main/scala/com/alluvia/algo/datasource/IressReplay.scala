package com.alluvia.algo.datasource

import com.alluvial.mdsclient.MDSClient
import com.alluvia.algo.EventAlgo
import com.alluvia.types.market.DayStart
import com.alluvia.types.ObservedEventPump
import com.alluvia.types.market.{DayStart, Start}
import com.alluvial.mdsclient.MDSClient._
import java.util.Date
import com.alluvialtrading.fix.{OrderTIF, OrderType, Order, OrderSide}

trait IressReplay extends MDSClient with EventAlgo {

  val eventPump = new ObservedEventPump
  override def printToBrowser = false
  override def delayBetweenTrades = 3.seconds
  override def maxOrders = 30
  val startDate: Date
  var totalOrders = 0
  val ORDER_LIMIT = 75
  val VALUE_LIMIT = 25000

  override def run {

    val runDate: java.util.Date = startDate
    println("prepping historical")
    val historicalPump = new ObservedEventPump
    historicalPump.addObserver(this)
    if (benchmarkDays > 0) {
      println("benchmarkng")
      val startBenchmarkDate = getLastTransactionDate(startDate.startOfDay)
      connector.getAllEvents(getMarketName, historicalPump,
        lib.combineDateTime(getTradingDate(startBenchmarkDate, -benchmarkDays + 1), "00:00:00.001"), lib.combineDateTime(startBenchmarkDate, "23:59:59.999"), true)
    }

    println("initialising")
    eventPump.addObserver(this)
    eventPump.addEvent(new Start)
    eventPump.addEvent(new DayStart(runDate))
    try {
      val securities: Array[String] = Array[String]("*")
      println("connecting")
			subscribeForReplay(runDate.toDateStr);
			waitWhileReceiving();
			disconnect();
    }
    catch {
      case ex: Exception => {
        disconnect()
        ex.printStackTrace()
      }
    }

    println("Exiting run in Live")
  }
  override def generateOrder(side: OrderSide, symbol: String, limit: Double, quantity: Int): Order = {

    if (totalOrders > ORDER_LIMIT) {
      println("Critical: order limit exceeded")
      System.exit(0)
    }

    if (limit * quantity / getBrokerCurrencyMultiplier > VALUE_LIMIT) {
      println("Critical: value limit exceeded. Exiting")
      System.exit(0)
    }

    // FIX check
    totalOrders += 1

    println("Total orders: " + totalOrders)

    val order: Order = new Order

    order.setSide(side)
    order.setType(OrderType.LIMIT)
    order.setTIF(OrderTIF.DAY)
    order.setSymbol(symbol)
    order.setLimit(limit)
    order.setQuantity(quantity)
    order.setOpen(order.getQuantity)
    order.setSecurityID(getISIN(symbol))
    order.setIdSource("4")
    order.setSecurityExchange(getSecurityExchange)
    order.setCurrency(getCurrency)
    var orderType: OrderType = order.getType


    if (orderType == OrderType.STOP || orderType == OrderType.STOP_LIMIT)
      order.setStop(200.0)

    order

  }
  override protected def onEvent(obj: AnyRef) {
    eventPump.addEvent(obj)
  }

}