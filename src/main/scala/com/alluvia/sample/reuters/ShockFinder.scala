package com.alluvia.sample.reuters

import com.alluvia.algo.EventAlgo
import java.util.Date
import collection.mutable.{ListBuffer, HashMap}
import com.alluvia.types.FixedList
import com.alluvia.types.market.{Trade, Quote}

trait ShockFinder
  extends EventAlgo {

  val lastTrade = new HashMap[String, BlimTrade]
  val lastQuoteTime = new HashMap[String, Date]
  var lastEvent: Any = ""
  val potentialShocks = new HashMap[String, FixedList[Any]]
  val listSize = 14
  val stocks = List("AAL.L",
"ABF.L",
"ADM.L",
"AGK.L",
"AMEC.L",
"ANTO.L",
"ARM.L",
"ASHM.L",
"AU.L",
"AV.L",
"AZN.L",
"BA.L",
"BARC.L",
"BATS.L",
"BG.L",
"BLND.L",
"BLT.L",
"BNZL.L",
"BP.L",
"BRBY.L",
"BSY.L",
"BT-A.L",
"CCL.L",
"CNA.L",
"CNE.L",
"CPG.L",
"CPI.L",
"CSCG.L",
"DGE.L",
"EMG.L",
"ENRC.L",
"ESSR.L",
"EXPN.L",
"FRES.L",
"GFS.L",
"GKN.L",
"GLEN.L",
"GSK.L",
"HL.L",
"HMSO.L",
"HSBA.L",
"IAG.L",
"IAP.L",
"IHG.L",
"IMI.L",
"IMT.L",
"INVP.L",
"IPR.L",
"ISAT.L",
"ITRK.L",
"ITV.L")

  val stockHash = new HashMap[String, Boolean]
  stocks.foreach(stockHash.put(_, true))
println("done")

  override def onQuote(q: Quote) {
/**
    if (!stocks.contains(q.security + "." + getSecurityExchange)) return
    // Ignore immediately after
    if (lastEvent.isInstanceOf[BlimTrade] &&
        date - lastEvent.asInstanceOf[BlimTrade].date < 1) return
    if (!potentialShocks.contains(security)) potentialShocks.put(security, new FixedList[Any](listSize))
    if (!lastQuoteTime.contains(security))  lastQuoteTime.put(security, new Date)

    if (date.getTime == lastQuoteTime(security).getTime) return
    lastQuoteTime.put(security, date)

    val events = potentialShocks(security).toList
    events match {
      case List(_: Blim, _: Blim, _: Blim, _: BlimTrade, _: Blim, _: Blim, _: Blim, _: Blim, _: Blim, _: Blim, _: Blim, _: Blim, _: Blim, _: Blim) =>
        val preShock = events(2).asInstanceOf[Blim]
        val postShock = events(5).asInstanceOf[Blim]
        val midShock = events(9).asInstanceOf[Blim]
        val last = events(13).asInstanceOf[Blim]
        val trade = events(3).asInstanceOf[BlimTrade]
//
        if (last.ask - last.bid < 0.6 * (postShock.ask - postShock.bid) &&
          (midShock.ask > last.ask || midShock.bid < last.bid) &&
         postShock.ask > preShock.ask && postShock.bid < preShock.bid &&
          (postShock.ask - postShock.bid) / trade.price > 0.00175 &&
//          (last.ask - last.bid) / trade.price < 0.0025 &&
          last.date - preShock.date > 5.seconds &&
          last.date - preShock.date < 5.minutes) {
          if (defined(preShock.ask) && defined(preShock.bid)) {
            println(last.date - preShock.date)
            events.foreach(println)
           //System.exit(0)
          }
//
        }
      case _ =>
    }


    // Blim
    if (bid != bidBefore || ask != askBefore) {
      val blim = Blim(date, security, bid, ask, volume, bidOrAsk)
      potentialShocks(security).append(blim)
    }
    else if (date.toTimeStr > "07:01") {
      val b = bid
      val bb = bidBefore
      val a = ask
      val ab = askBefore
      val noblim = Blim(date, security, bid, ask, volume, bidOrAsk)
      potentialShocks(security).append(noblim)
      //println("possible error")
      //System.exit(0)
    }
*/
  }

  override def onTrade(t: Trade) {

    if (!stocks.contains(t.security + "." + getSecurityExchange)) return
    if (!potentialShocks.contains(t.security)) {
      val first = new FixedList[Any](listSize)
      first.append(BlimTrade(t.date, t.security, t.price, t.volume))
      potentialShocks.put(t.security, first)
      return
    }

    val fixedList = potentialShocks(t.security)
    val lastEvent = fixedList.tail
    lastEvent match {
      case lastEvent: BlimTrade =>
        if (t.date - lastEvent.date < 1.second) {
          potentialShocks(t.security).trimEnd(1)
          potentialShocks(t.security).append(BlimTrade(t.date, t.security, t.price, t.volume + lastEvent.volume))
        }
      case _ => potentialShocks(t.security).append(BlimTrade(t.date, t.security, t.price, t.volume))
    }
  }

  case class BlimTrade(date: Date, security: String, price: Double, volume: Double) {override def toString = "T: " + security + price + " " + date}
  case class Blim(date: Date, security: String, bid: Double, ask: Double, volume: Double, bidOrAsk: String) {override def toString = security + " " + bid + " " + ask + " " + date + " " + bidOrAsk}
  case class NoBlim(date: Date, security: String, price: Double, volume: Double) {override def toString = "NB"}

}