package com.alluvia.tools.dataquality

import com.alluvia.algo.EventAlgo
import com.alluvia.types.market.{Quote, QuoteFull}

trait EventStreamer
  extends EventAlgo {

  // Parameters
  // ------------------------------------------------------------

  // User params
  val print2file = true // 'true' or 'false'
  val loud = true // 'true' or 'false'
  val debugSecurity = "BHP" // "", "BLT.L", "BHP", etc.
  def print2Console(security: String) = (loud && ("" == debugSecurity || security == debugSecurity))

  // onQuote
  // ------------------------------------------------------------
//
//  override def onQuote(q: Quote) {
//
//    // Debug
//    //if (print2Console(q.security)) println(dateToISODateTimeString(q.date) + " " + q.security + " " + transType)
//
//    // Print console message
//    if (print2Console(q.security)) {
//      println(dateToISODateTimeString(q.date) + " " + q.security + " " + transType +
//        "\t--- $" + q.price + " " + q.volume.toInt + "x $" + round2(q.value) + " " + q.bidOrAsk +
//        "\t--- $" + q.bid + " / $" + q.ask + " (" + q.spreadBps + ") : " + q.bidVol.toInt + "x / " + q.askVol.toInt + "x" +
//        "\t--- Before: $" + q.bidBefore + " / $" + q.askBefore + " : " + q.bidVolBefore.toInt + "x / " + q.askVolBefore.toInt + "x")
//    }
//
//  }


  override def onQuoteFull(q: QuoteFull) {

    // Print console message
    if (print2Console(q.security)) {
      println(q.security + " " + transType +
        "\t--- Open: $" + q.openPrice + " High: $" + q.maxPrice + " Low: $" + q.minPrice + " Close: $" + q.closePrice +
        "\t--- SecurityStatus" + q.securityStatus)
    }

  }

}