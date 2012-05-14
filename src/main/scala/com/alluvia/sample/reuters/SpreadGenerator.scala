package com.alluvia.sample.reuters

import java.util.Date
import com.alluvia.algo.EventAlgo
import com.alluvia.types.market.{Trade, QuoteMatch, Quote}

trait SpreadGenerator extends EventAlgo {

  val dateString = "2011-01-06"
  val currentDate: Date = "2011-01-06"
  val fileName = dateString + ".csv"
  val securityMatch = "SVW"
  // ON TRADE
  override def onTrade(t: Trade) {
    if (t.date > currentDate) {
    if (t.security == securityMatch)
      printcsv("trades.csv", t.date.toIso, t.date.toTimeStr, t.volume, (t.price * getBrokerCurrencyMultiplier ))

    }

  }

  override def onQuote(q: Quote) {
    
    if (q.date.toTimeStr > "06:59") {
      if (q.security == securityMatch) {
        if (q.bidOrAsk == 'B') {
          printcsv("bids.csv", q.date.toIso, q.date.toTimeStr, q.volume, (q.price * getBrokerCurrencyMultiplier ))
         // printcsv("spreads.csv", date.toIso, date.toTimeStr, volume, (askBefore * getBrokerCurrencyMultiplier ))
        }
        else {
          printcsv("spreads.csv", q.date.toIso, q.date.toTimeStr, q.volume, (q.price * getBrokerCurrencyMultiplier ))
          printcsv("asks.csv", q.date.toIso, q.date.toTimeStr, q.volume, (q.askBefore * 1.05 * getBrokerCurrencyMultiplier))
        }

      }
    }

  }


  override def onQuoteMatch(q: QuoteMatch) {
    if (q.price < 0.5) {
      println(q.security + " " + q.price)
    }
    /**
    if (quoteMatch.UpdateTime > currentDate) {
      printcsv(fileName, "MATCH", quoteMatch.UpdateTime.toDateTime, quoteMatch.Security + "." + getSecurityExchange,
        "E", quoteMatch.IndicativePrice, quoteMatch.MatchVolume.toInt, quoteMatch.IndicativePrice * quoteMatch.MatchVolume, "B")
    }*/
  }

}
