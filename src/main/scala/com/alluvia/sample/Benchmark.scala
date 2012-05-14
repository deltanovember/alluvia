package com.alluvia.sample

import com.alluvia.algo.EventAlgo
import collection.mutable.HashMap
import java.util.Date
import com.alluvia.types.{MagicMap}
import com.alluvia.types.market.{DayEnd, DayStart, Trade}

trait Benchmark extends EventAlgo {

  //date: Date, security: String, open: Double, openVolume: Int, close: Double, closeVolume: Int, high: Double, low: Double, tCount: Int, volume: Double, value: Double

  val open = new HashMap[Security, Double]
  val openDate = new HashMap[Security, Date]
  val openVolume = MagicMap[Security](0.0)
  val close = new HashMap[Security, Double]
  val closeVolume = MagicMap[Security](0.0)

  val high = MagicMap[Security](Double.NegativeInfinity)
  val low = MagicMap[Security](Double.PositiveInfinity)
  val tCount = MagicMap[Security](0)
  val tradeVolume = MagicMap[Security](0)
  val tradeValue = MagicMap[Security](0.0)

  override def onDayStart(d: DayStart) {
    open.clear()
  }
  override def onTrade(t: Trade) {
    /**
    //println(security, price, volume)
    if (!open.contains(security)) {
      open.put(security, price)
      openDate.put(security, date)
    }
    if (date.equals(openDate(security))) {
      openVolume.put(security, volume + openVolume(security))
      if (security == "BHP") {
        println("here")
      }
    }
    if (date > closeTime) {
      close.put(security, price)
      closeVolume.put(security, volume + closeVolume(security))
    }
    if (price > high(security)) high.put(security, price)
    if (price < low(security)) low.put(security, price)
    tCount(security) += 1
    tradeVolume(security) += volume.toInt
    tradeValue(security) += value
     */
  }

  override def onDayEnd(d: DayEnd) {
    println(open("BHP"), openVolume("BHP"), close("BHP"), closeVolume("BHP"), high("BHP"), low("BHP"),
      tCount("BHP"), tradeVolume("BHP"), tradeValue("BHP"))
  }
  
}