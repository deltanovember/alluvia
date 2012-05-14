package com.alluvia.algo.datasource

import com.alluvia.types.market.{DayStart, Start}
import com.alluvia.algo.EventAlgo
import com.alluvialtrading.proquote.simplesocket.console.ProquoteConnector
import com.alluvia.types.ObservedEventPump

trait Proquote extends ProquoteConnector with EventAlgo with LiveData {

  val eventPump = new ObservedEventPump
  val replay = false
  override def maxOrders = 10

  override def run {

    println("prepping historical")
    val historicalPump = new ObservedEventPump
    historicalPump.addObserver(this)
    if (benchmarkDays > 0) {
      println("benchmarkng")
      /**
      connector.getAllEvents(getMarketName, historicalPump,
        lib.combineDateTime(getTradingDate(startDate, -benchmarkDays + 1), "00:00:00.001"),
        lib.combineDateTime(startDate, "23:59:59.999"), true)*/
    }

    println("initialising")
    eventPump.addObserver(this)
    eventPump.addEvent(new Start)
    eventPump.addEvent(new DayStart(new java.util.Date))
    try {

    }
    catch {
      case ex: Exception => {
        ex.printStackTrace
      }
    }

  }

  override protected def onEvent(obj: AnyRef): Unit = {
    eventPump.addEvent(obj)
  }

}