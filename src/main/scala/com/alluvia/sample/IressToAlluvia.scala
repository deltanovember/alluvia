package com.alluvia.sample

/**
 * Convert single day to Alluvial format
 */

import com.alluvia.algo.EventAlgo
import com.alluvia.types.market.{Quote, Trade, QuoteMatch}

trait IressToAlluvia extends EventAlgo {

  var replayDate = new java.util.Date()
  var file = ""
/**
  override def onDayStart {
    println("start" + date)
    replayDate = date
    file = date.toIso + ".csv"
    printcsv(file, "CONTROL", date.toDateTime, "", "", "", "", "", "STARTOFDAY")
  }

  override def onQuote(q: Quote) {
    if (validData)
      printcsv(file, "ENTORD", date, security, "E", price, volume.toInt, price.toDouble * volume.toDouble, getBidOrAsk)

  }

  override def onQuoteMatch(q: QuoteMatch) {
    if (validData)
      printcsv(file, "MATCH", date, security, "E", price, volume.toInt, price.toDouble * volume.toDouble, "")
  }
  override def onTrade(t: Trade) {
    if (validData)
      printcsv(file, "TRADE", date, security, "E", price, volume.toInt, price.toDouble * volume.toDouble, getBidOrAsk)
}

  def getBidOrAsk = if (bidOrAsk == "B") "B" else "S"
  def validData = date > replayDate.startOfDay && date < replayDate.endOfDay
*/
}