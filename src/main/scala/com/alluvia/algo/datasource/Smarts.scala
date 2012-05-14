package com.alluvia.algo.datasource

import smarts.rmi.client.FavReader
import com.alluvia.algo.EventAlgo
import java.util.Date
import com.alluvial.mdsclient.MDSClient._
import java.text.{SimpleDateFormat, DateFormat}
import smarts.rmi.contract.{DayEndArgs, TradeArgs}
import com.alluvia.types.market.{DayEnd, DayStart, Start}
import com.alluvia.types.ObservedEventPump

trait Smarts extends FavReader with EventAlgo {

  val eventPump = new ObservedEventPump

  override def printToBrowser = true
  override def delayBetweenTrades = 4.seconds
  override def maxOrders = 30
  val startDate: Date

  override def run {

    val runDate: java.util.Date = startDate
    println("initialising")
    eventPump.addObserver(this)
    eventPump.addEvent(new Start)
    eventPump.addEvent(new DayStart(runDate))
    try {
      println("connecting")
      var dfm: DateFormat = new SimpleDateFormat("dd/MM/yyyy")
      var startDate: Date = null
      startDate = dfm.parse("01/03/2011")
      var endDate: Date = dfm.parse("01/03/2011")
      var market: String = "asx_mq"
      super.run(startDate, endDate)
    }
    catch {
      case ex: Exception => {
        ex.printStackTrace()
      }
    }

    println("Exiting run in Smarts")
  }

  override def onDayEnd(args: DayEndArgs): Unit = {
    println("smarts day end" + new Date)
    eventPump.addEvent(new DayEnd(new Date))
  }
  override def onTrade(args: TradeArgs): Unit = {
    val trade = new com.alluvial.mds.contract.Trade(0, 0, 0, 0, 0, 0, 0.0, args.getVolume, args.getPrice * 100, (new Date).getTime, 0, "o", (new Date).getTime, 0)
    trade.Security = args.getSecurity.split("\\." + getSecurityExchange)(0)
    eventPump.addEvent(trade)
          //  trade.Security = processSecurity(result.getString("SECURITY"))
   // System.out.println("[DEBUG_CLIENT] - onTrade: " + (new Date).toString + " " + cnt)
  }

//  def onQuote(args: QuoteArgs): Unit = {
//    if (args.security == "BHP.AX") {
//    }
//  }

}