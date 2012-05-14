package com.alluvia.sample

import scala.math.log
import com.alluvia.algo.{EventAlgo, BackTestingAlgo}
import java.util.Date
import collection.mutable.{ListBuffer, HashMap}
import com.alluvia.types.market.{Trade, Start, DayStart, DayEnd}

abstract class RExtractor(val startDate: Date, val endDate: Date)
  extends EventAlgo
  with BackTestingAlgo {

  val lastTime = getCloseTime
  override def benchmarkDays = 5
  var benchmarkEvents = 20
  var sunsetTime1 = "14:00:00.000"
  var sunsetTime2 = "15:00:00.000"
  var sunsetTime3 = "15:30:00.000"
  var sunsetTime4 = "16:00:00.000"
  var sunsetTime5 = "16:15:00.000"
  var sunsetTime6 = "16:25:00.000"

  var time1 = new Date
  var time2 = new Date
  var time3 = new Date
  var time4 = new Date
  var time5 = new Date
  var time6 = new Date
  var vwapStart = new Date
  var vwapEnd = new Date

  val recent = new HashMap[String, ListBuffer[Trade]]
  // Maximum prices for times of day
  val maxPrices = new HashMap[String, Double]
  val minPrices = new HashMap[String, Double]

  // hash securities to value, volume
  val vwaps = new HashMap[String, (Double, Int)]

  // store historical data for csv
  val historical = new ListBuffer[String]

  def getKey(security: String, time: String) = time + security

  val filename = "sunset6.csv"
/**
  def expiry = if (date.toDateStr == "20100716" ||
    date.toDateStr == "20100820" ||
    date.toDateStr == "20100917" ||
    date.toDateStr == "20101015" ||
    date.toDateStr == "20101119" ||
    date.toDateStr == "20101217" ||
    date.toDateStr == "20110121" ||
    date.toDateStr == "20110218" ||
    date.toDateStr == "20110318") 1
  else 0

  def monthEnd = if (date.toDateStr == "20100630" ||
    date.toDateStr == "20100730" ||
    date.toDateStr == "20100831" ||
    date.toDateStr == "20100930" ||
    date.toDateStr == "20101029" ||
    date.toDateStr == "20101130" ||
    date.toDateStr == "20101231" ||
    date.toDateStr == "20110131" ||
    date.toDateStr == "20110228" ||
    date.toDateStr == "20110331") 1
  else 0

  override def onDayEnd {

    println("dayend")

    for (security <- allSecurities) {

      // Spread last percent
      val askLast = askBefore
      val bidLast = bidBefore
      val midLast = (askBefore + bidBefore) / 2

      val closePrice = price

      val spreadLast = round4(askLast - bidLast)
      val spreadLastPercent = round2(100 * spreadLast / midLast)

      // Indicative uncrossing price change and value
      val auctionDelta = round2(100 * log(closePrice / midLast))

      // Sunset change based on events
      val recentTrades = recent(security).sortBy(_.price)
      val maxPriceEvent = recentTrades.last.price
      val minPriceEvent = recentTrades.head.price
      val eventDelta = round4(2 * 100 * (maxPriceEvent - minPriceEvent) / (maxPriceEvent + minPriceEvent) + 0.0001)

      val key1 = getKey(security, sunsetTime1)
      val key2 = getKey(security, sunsetTime2)
      val key3 = getKey(security, sunsetTime3)
      val key4 = getKey(security, sunsetTime4)
      val key5 = getKey(security, sunsetTime5)
      val key6 = getKey(security, sunsetTime6)

      if (maxPrices.contains(key1)) {
        val maxPriceTime1 = maxPrices(key1)
        val minPriceTime1 = minPrices(key1)
        val timeDelta1 = round4(2 * 100 * (maxPriceTime1 - minPriceTime1) / (maxPriceTime1 + minPriceTime1) + 0.0001)

        val maxPriceTime2 = maxPrices(key2)
        val minPriceTime2 = minPrices(key2)
        val timeDelta2 = round4(2 * 100 * (maxPriceTime2 - minPriceTime2) / (maxPriceTime2 + minPriceTime2) + 0.0001)

        val maxPriceTime3 = maxPrices(key3)
        val minPriceTime3 = minPrices(key3)
        val timeDelta3 = round4(2 * 100 * (maxPriceTime3 - minPriceTime3) / (maxPriceTime3 + minPriceTime3) + 0.0001)

        val maxPriceTime4 = maxPrices(key4)
        val minPriceTime4 = minPrices(key4)
        val timeDelta4 = round4(2 * 100 * (maxPriceTime4 - minPriceTime4) / (maxPriceTime4 + minPriceTime4) + 0.0001)

        val maxPriceTime5 = maxPrices(key5)
        val minPriceTime5 = minPrices(key5)
        val timeDelta5 = round4(2 * 100 * (maxPriceTime5 - minPriceTime5) / (maxPriceTime5 + minPriceTime5) + 0.0001)

        val maxPriceTime6 = maxPrices(key6)
        val minPriceTime6 = minPrices(key6)
        val timeDelta6 = round4(2 * 100 * (maxPriceTime6 - minPriceTime6) / (maxPriceTime6 + minPriceTime6) + 0.0001)

        val vwapLast = vwaps.get(security) match {
          case Some(vwap) => vwap._1 / vwap._2
          case None => midLast
        }

        val auctionDeltaVwap = round2(100 * log(closePrice / vwapLast))

        printcsv(filename,
          date.toDateStr,
          security,
          auctionDelta,
          auctionDeltaVwap,
          spreadLastPercent,
          expiry,
          monthEnd,
          eventDelta,
          timeDelta1,
          timeDelta2,
          timeDelta3,
          timeDelta4,
          timeDelta5,
          timeDelta6
          // overnight
        )
      }


      /**

      // Print data to csv
      // ------------------------------------------------------------------------------

      var next_price = getVWAP(getTradingDate(date, 1), "09:00:00.000", "11:00:00.000")
      if (0 == next_price)
        next_price = getVWAP(getTradingDate(date, 1), "11:00:00.000", "13:00:00.000")
      if (0 == next_price)
        next_price = getVWAP(getTradingDate(date, 1), "15:00:00.000", "17:00:00.000")

      val overnight = round2(100 * log(next_price / closePrice))

       */


      println(security + ":" + bidLast + " " + askLast + " " + closePrice)
    }

    /**
    if (abs(overnight) <= 10)

     */
  }

  override def onDayStart {

    time1 = lib.combineDateTime(date, sunsetTime1)
    time2 = lib.combineDateTime(date, sunsetTime2)
    time3 = lib.combineDateTime(date, sunsetTime3)
    time4 = lib.combineDateTime(date, sunsetTime4)
    time5 = lib.combineDateTime(date, sunsetTime5)
    time6 = lib.combineDateTime(date, sunsetTime6)

    vwapStart = lib.combineDateTime(date, "16:25:00.000")
    vwapEnd = lib.combineDateTime(date, "16:30:00.000")

  }

  override def onStart(start: Start) {

    // Display
    println("\nExtractor ! " + lastTime)
    println("\n======================================================\n\n")

    // Write output file headers
    printcsv(filename, "date", "stock", "delta", "delta2", "spread", "expiry", "month_end", "event_delta", "time_delta1", "time_delta2", "time_delta3", "time_delta4", "time_delta5", "time_delta6", "overnight")

  }

  override def onTrade(t: Trade) {
/**
    // Store recent trades
    if (!recent.contains(security)) {
      recent.put(security, ListBuffer(trade))
    }
    else {
      val recentTrades = recent(security)
      if (recentTrades.size == 20) {
        recentTrades.trimStart(1)
      }
      recentTrades.append(trade)
      recent.put(security, recentTrades)
    }

    // Max and min prices
    if (trade.TradeTime.before(time1)) {
      updatePrices(trade, sunsetTime1)
    }
    if (trade.TradeTime.before(time2)) {
      updatePrices(trade, sunsetTime2)
    }
    if (trade.TradeTime.before(time3)) {
      updatePrices(trade, sunsetTime3)
    }
    if (trade.TradeTime.before(time4)) {
      updatePrices(trade, sunsetTime4)
    }
    if (trade.TradeTime.before(time5)) {
      updatePrices(trade, sunsetTime5)
    }
    if (trade.TradeTime.before(time6)) {
      updatePrices(trade, sunsetTime6)
    }

    // Store vwaps
 */
  }


  def monthEnd(dateStr: String): Int = {
    if (dateStr == "20100630" ||
      dateStr == "20100730" ||
      dateStr == "20100831" ||
      dateStr == "20100930" ||
      dateStr == "20101029" ||
      dateStr == "20101130" ||
      dateStr == "20101231" ||
      dateStr == "20110131" ||
      dateStr == "20110228" ||
      dateStr == "20110331") 1
    else 0
  }

   def storeHistorical(args: Any*) {
    val line = args.mkString(",")
    historical.append(line)
  }

  def updatePrices(trade: Trade, time: String) {

    val key = getKey(security, time)
    maxPrices.get(key) match {
      case Some(oldPrice) => if (oldPrice < trade.price) maxPrices.put(key, trade.price)
      case None => maxPrices.put(key, trade.price)
    }

    minPrices.get(key) match {
      case Some(oldPrice) => if (oldPrice > trade.price) minPrices.put(key, trade.price)
      case None => minPrices.put(key, trade.price)
    }

  }

  def updateVwaps(trade: Trade) {

    vwaps.get(trade.security) match {
      case Some(vwap) => vwaps.put(trade.security, (trade.price * trade.volume + vwap._1, trade.volume.toInt + vwap._2))
      case None => vwaps.put(trade.security, (trade.price * trade.volume, trade.volume.toInt))
    }

  }*/
}
