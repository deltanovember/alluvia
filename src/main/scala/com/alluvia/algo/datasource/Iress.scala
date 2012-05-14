package com.alluvia.algo.datasource

import com.alluvial.mdsclient.MDSClient
import com.alluvia.types.ObservedEventPump
import com.alluvia.types.market.{DayStart, Start}
import com.alluvia.algo.EventAlgo

trait Iress extends MDSClient with EventAlgo with LiveData {

  val eventPump = new ObservedEventPump
  val replay = false
  override def delayBetweenTrades = 3.seconds
  override def maxOrders = 50

  override def run {

    val replayDate = "2011-09-15"
    val runDate: java.util.Date =
      if (replay) {
        println("replaying")
        replayDate

      } else new java.util.Date()
    val startDate = getLastTransactionDate(replayDate.startOfDay)
    println("prepping historical")
    val historicalPump = new ObservedEventPump
    historicalPump.addObserver(this)
    if (benchmarkDays > 0) {
      println("benchmarkng")
      connector.getAllEvents(getMarketName, historicalPump,
        lib.combineDateTime(getTradingDate(startDate, -benchmarkDays + 1), "00:00:00.001"), lib.combineDateTime(startDate, "23:59:59.999"), true)
    }

    println("initialising")
    eventPump.addObserver(this)
    eventPump.addEvent(new Start)
    eventPump.addEvent(new DayStart(runDate))
    try {
      val securities: Array[String] = Array[String]("*")
      println("connecting")
			subscribeForLive(securities);
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

  override protected def onEvent(obj: AnyRef) {
    eventPump.addEvent(obj)
  }

}