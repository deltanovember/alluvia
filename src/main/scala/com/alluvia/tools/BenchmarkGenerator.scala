package com.alluvia.tools

import java.util.Date

import com.alluvia.markets._
import com.alluvia.database.BackTestingLib
import com.alluvialtrading.data.Quote
import com.alluvialtrading.tools.TraderLib

object BenchmarkGenerator {
  def main(args: Array[String]) {
    new BenchmarkGenerator with LSE
  }
}


abstract class BenchmarkGenerator extends BackTestingLib with Market {

  val library = new TraderLib
  var start: Date = library.combineDateTime("2011-04-01", "00:00:01.000")
  var end: Date = getLastTradeDate
  var currentDate: String = library.dateToISODateString(start)
  var endString: String = library.dateToISODateString(end)
  var date: Date = null
  while (!(currentDate == endString)) {
    val allSecurities: Array[String] = getAllSecurities(getMarketName, currentDate)
    for (rawSecurity <- allSecurities) {
      val security = rawSecurity.trim
      date = library.convertISODateTimeString(currentDate)
      val endOfDay: Date = library.combineDateTime(date, "23:59:59.000")
      val open: Double = getOpenPrice(getMarketName, security, date)
      val close: Double = getClosePrice(getMarketName, security, date)
      val precloseQuote: Quote = getQuote(getMarketName, security, currentDate, getCloseTime)
      val preclose: Double =  if (null != precloseQuote) precloseQuote.getMid else 0.0

      val high: Double = getMaxPrice(getMarketName, security, date, endOfDay)
      val low: Double = getMinPrice(getMarketName, security, date, endOfDay)
      val tcount: Int = getTcount(getMarketName, security, date, endOfDay)
      val volume: Int = getVolume(getMarketName, security, date, endOfDay, 0)
      val value: Double = getValue(getMarketName, security, date, endOfDay, 0)
      val minSpread: Double = getMinSpread(getMarketName, security, currentDate, currentDate, false)
      System.out.println(currentDate + " " + security)
      insertBenchmark(getMarketName, currentDate, security, open, close, preclose, high, low, tcount, volume, value, minSpread)
    }
    currentDate = getTradingDate(getMarketName, currentDate, 1)
  }

}

