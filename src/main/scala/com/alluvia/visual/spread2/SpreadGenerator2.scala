package com.alluvia.visual.spread2

import java.util.Date
import com.alluvia.algo.EventAlgo
import io.Source
import java.io.File
import java.text.DecimalFormat
import java.math.RoundingMode
import com.alluvia.types.market._
import com.alluvia.types.MagicMap
import collection.mutable.{HashMap}

trait SpreadGenerator2 extends EventAlgo {

  println("Running IRESS Data Converter");

  // ===================================================================================================================
  // User Parameters
  // ===================================================================================================================

  val runDate = "20111128"
  val spreadData = false  // Generate Spread data or Historical data
  val dirLocal = "C:/TEMP/iress/"
  val fileNameLocal = "iress_"  // iress_yyyymmdd.csv
  val dir = "C:/Users/mclifton.CM-CRC/Alluvial/Code/alluvia/src/main/scala/com/alluvia/visual/spread2/spreaddata/"
  val serverDir = "profit/spread/"
  val fileNameHtml = "index.html"
  val fileNameSpread = "spread/spread_"  // data/yyyymmdd/spread/spread_SEC.csv
  val fileNameConfigSecurity = "config/config_security.csv"  // data/yyyymmdd/config/config_security.csv
  val fileNameConfigMarket = "config_market.csv" // data/config_market.cs

  // New config
  val start = "09:45:00.000"
  val openAuctionStart = "09:50:00.000"
  val closeAuctionStart = "16:00:00.000"
  val end = "16:15:00.000"
  val randomTime = 15 //seconds
  val openAuctionEndDefault = "10:05:00.000"
  val closeAuctionEndDefault = "16:10:00.000"

  // Define
  var lastBid = MagicMap[String](0.0);
  var lastAsk = MagicMap[String](0.0);
  var lastBidVol = MagicMap[String](0.0);
  var lastAskVol = MagicMap[String](0.0);
  var lastBidValue = MagicMap[String](0.0);
  var lastAskValue = MagicMap[String](0.0);
  val securityStatus = MagicMap[String]('P');
  val openUncrossingTrade = MagicMap[String](false);
  val closeUncrossingTrade = MagicMap[String](false);
  val securityMatch = MagicMap[String](false);
  val securityOpen = MagicMap[String](false);

  val isActive = new HashMap[String, Boolean]
  val openAuctionEndDerivedDate = new HashMap[String, Date]
  val openAuctionEndDerivedTimeStr = new HashMap[String, String]
  val closeAuctionEndDerivedTimeStr = new HashMap[String, String]
  val firstUncrossing = new HashMap[String, Date]
  var tagFields = ' '
  var transId = 0
  var atStart = false
  var dateToday = new Date()

  // Securities
  securityMatch.put("SVW", true)
  securityMatch.put("BHP", true)
  val iressData = dirLocal + fileNameLocal + runDate + ".csv"


  // ===================================================================================================================
  // Generate Chart Data
  // ===================================================================================================================

  // On quoteFull
  override def onQuoteFull(q: QuoteFull) {

    if(q.security == "BHP") println("QUOTE_FULL: " + q.date.toIso + " " + q.date.toTimeStr + " " + q.security)

    if (!(q.date.toDateStr == runDate)) return

    //if (!securityMatch(q.security) == true) return

    // HACK!!
    val realStatus = if (q.securityStatus.toInt == 0) 'P' else q.securityStatus

    // At start
    // ------------------------------------------------------------
    if (atStart == false) {

      println("AT START")
      println("==================================================")

      // Market config file
      if (spreadData == true) {
        val fileNameMarket = dir + "data/" + fileNameConfigMarket
        printcsv(fileNameMarket, "start", start)
        printcsv(fileNameMarket, "openAuctionStart", openAuctionStart)
        printcsv(fileNameMarket, "closeAuctionStart", closeAuctionStart)
        printcsv(fileNameMarket, "end", end)
        printcsv(fileNameMarket, "randomTime", randomTime)
        printcsv(fileNameMarket, "brokerCurrencyMultiplier", getBrokerCurrencyMultiplier)
      }
      else {
        printcsv(iressData,
          "CONTROL",
          q.date.toIso + " 00:00:00.001",
          "",
          "",
          "",
          "",
          "",
          "",
          "STARTOFDAY")
      }

      atStart = true

    }

    // At day start
    // ------------------------------------------------------------
    if (q.date.hours <= 9 && realStatus == 'P' && securityOpen(q.security) == false) {

      if(q.security == "BHP") {
        println("AT DAY START")
        println("==================================================")
      }

      securityOpen.put(q.security, true)

      // Define
      lastBid.clear()
      lastAsk.clear()
      lastBidVol.clear()
      lastAskVol.clear()
      lastBidValue.clear()
      lastAskValue.clear()
      securityStatus.clear()
      openUncrossingTrade.clear()
      closeUncrossingTrade.clear()

      isActive.clear()
      openAuctionEndDerivedDate.clear()
      openAuctionEndDerivedTimeStr.clear()
      closeAuctionEndDerivedTimeStr.clear()
      firstUncrossing.clear()
      tagFields = ' '
      transId = 0
      dateToday = q.date

    }

    // Reset uncrossing trade classifier
    if (openAuctionEndDerivedDate.contains(q.security) && (q.date - openAuctionEndDerivedDate(q.security)) > 1.second) openUncrossingTrade.put(q.security, false)

    // IF open auction has just ended THEN next trade may be an uncrossing trade
    if (securityStatus(q.security) != ' ' && realStatus == ' ') {
      openUncrossingTrade.put(q.security, true)
    }

    // IF an auction has just ended THEN next trade may be an uncrossing trade
    if (securityStatus(q.security) == 'U' || securityStatus(q.security) == 'H' || securityStatus(q.security) == 'O') {
      closeUncrossingTrade.put(q.security, true)
    }

    // IF open auction has ended AND open auction end time is undefined THEN set open auction end time
    if ((securityStatus(q.security) == 'P' && realStatus == ' ') && !openAuctionEndDerivedTimeStr.contains(q.security)) {
      openAuctionEndDerivedDate.put(q.security, q.date)
      openAuctionEndDerivedTimeStr.put(q.security, q.date.toTimeStr)
    }

    // IF close auction has ended THEN set close auction end time
    if (securityStatus(q.security) == 'U' && realStatus == 'O') {
      closeAuctionEndDerivedTimeStr.put(q.security, q.date.toTimeStr)
    }

    // Filename
    val fileName = if (spreadData == true) dir + "data/" + q.date.toDateStr.replace("-","") + "/" + fileNameSpread + q.security + ".csv" else iressData

    // IF trading has stopped THEN
    if (securityStatus(q.security) == ' ' && (realStatus == 'U' || realStatus == 'H')) {

      // Insert fictitious quotes at end of trading
      for (i <- 1 to 2) {
        transId += 1;
        printcsv(fileName,
          "ENTORD",
          q.date.toIso + " " + q.date.toTimeStr,
          q.security,
          q.category,
          transId,
          lastAsk(q.security),
          lastAskVol(q.security).toInt,
          lastAskValue(q.security),
          'A')
        transId += 1;
        printcsv(fileName,
          "ENTORD",
          q.date.toIso + " " + q.date.toTimeStr,
          q.security,
          q.category,
          transId,
          lastBid(q.security),
          lastBidVol(q.security).toInt,
          lastBidValue(q.security),
          'B')
      }

    }

    // At day end
    // ------------------------------------------------------------
    if (spreadData == true && q.date.hours >= 16 && q.date.minutes >= 12 && securityOpen(q.security) == true) {

      if(q.security == "BHP") {
        println("AT DAY END")
        println("==================================================")
      }

      securityOpen(q.security) == false

      // Security config files
      val openAuctionEnd = if (!openAuctionEndDerivedTimeStr.contains(q.security)) getOpenAuctionEnd(q.security) else openAuctionEndDerivedTimeStr(q.security)
      val closeAuctionEnd = if (!closeAuctionEndDerivedTimeStr.contains(q.security)) closeAuctionEndDefault else closeAuctionEndDerivedTimeStr(q.security)

      val fileNameSecurity = dir + "data/" + q.date.toDateStr.replace("-","") + "/" + fileNameConfigSecurity
      printcsv(fileNameSecurity, q.security, "openAuctionEnd", openAuctionEnd)
      printcsv(fileNameSecurity, q.security, "closeAuctionEnd", closeAuctionEnd)

    }

//    // Print current control message
//    transId += 1;
//    printcsv(fileName,
//      q.transType,
//      q.date.toIso + " " + q.date.toTimeStr,
//      q.security,
//      q.category,
//      transId,
//      "",
//      "",
//      "",
//      realStatus)

    // Update security status
    securityStatus.put(q.security, realStatus)

  }

  // On trade
  override def onTrade(t: Trade) {

    if (t.security == "BHP") println("TRADE: " + t.date.toIso + " " + t.date.toTimeStr + " " + t.security)

    if (!(t.date.toDateStr == runDate)) return

    // IF security does not match THEN break
    //if (!securityMatch(t.security) == true) return

    // Add security to active list
    if (!isActive.contains(t.security)) isActive.put(t.security, true)

    // IF open auction end time is undefined (because status message is not in sequence) THEN
    // set open auction end time and classify next trades as uncrossing
    if (!openAuctionEndDerivedTimeStr.contains(t.security)) {
      openAuctionEndDerivedDate.put(t.security, t.date)
      openAuctionEndDerivedTimeStr.put(t.security, t.date.toTimeStr)
      openUncrossingTrade.put(t.security, true)
    }

    // IF this is the first uncrossing trade THEN store the uncrossing time
    if (tagFields == 'U' && !firstUncrossing.contains(t.security)) firstUncrossing.put(t.security, t.date)

    // IF last trade was an uncrossing trade AND current timestamp is not the same THEN assume this trade is not an uncrossing trade
    if (firstUncrossing.contains(t.security) && t.date != firstUncrossing(t.security)) openUncrossingTrade.put(t.security, false)

    // Set trade flag
    if (openUncrossingTrade(t.security) == true || closeUncrossingTrade(t.security) == true) tagFields = 'U' else tagFields = t.tagFields

    // Filename
    val fileName = if (spreadData == true) dir + "data/" + t.date.toDateStr.replace("-","") + "/" + fileNameSpread + t.security + ".csv" else iressData

    transId += 1;
    printcsv(fileName,
      t.transType,
      t.date.toIso + " " + t.date.toTimeStr,
      t.security,
      t.category,
      transId,
      t.price * getBrokerCurrencyMultiplier,
      t.volume.toInt,
      round(t.value * getBrokerCurrencyMultiplier, 2),
      tagFields)

  }

  // On quote
  override def onQuote(q: Quote) {

    if (q.security == "BHP") println("QUOTE: " + q.date.toIso + " " + q.date.toTimeStr + " " + q.security)

    if (!(q.date.toDateStr == runDate)) return

    // IF security does not match THEN break
    //if (!securityMatch(q.security) == true) return

    // IF not continuous trading THEN break
    if (securityStatus(q.security) != ' ') return

    // Reset uncrossing trade classifier
    if (openAuctionEndDerivedDate.contains(q.security) && (q.date - openAuctionEndDerivedDate(q.security)) > 1.second) openUncrossingTrade.put(q.security, false)

    // Filename
    val fileName = if (spreadData == true) dir + "data/" + q.date.toDateStr.replace("-","") + "/" + fileNameSpread + q.security + ".csv" else iressData

    // Print quote data
    transId += 1;
    printcsv(fileName,
      q.transType,
      q.date.toIso + " " + q.date.toTimeStr,
      q.security,
      q.category,
      transId,
      q.price * getBrokerCurrencyMultiplier,
      q.volume.toInt,
      round(q.value * getBrokerCurrencyMultiplier, 2),
      q.tagFields)

    // Last values
    if (q.tagFields == 'A') {
      lastAsk.put(q.security, q.price * getBrokerCurrencyMultiplier);
      lastAskVol.put(q.security, q.volume);
      lastAskValue.put(q.security, round(q.value * getBrokerCurrencyMultiplier, 2));
    }
    else {
      lastBid.put(q.security, q.price * getBrokerCurrencyMultiplier);
      lastBidVol.put(q.security, q.volume);
      lastBidValue.put(q.security, round(q.value * getBrokerCurrencyMultiplier, 2))
    }

  }

  def round(unrounded: Double): Double = {
    var df: DecimalFormat = new DecimalFormat("#.##")
    df.setRoundingMode(RoundingMode.HALF_UP)
    df.format(unrounded).toDouble
  }

  def getOpenAuctionEnd(security : String) : String = {

    val openAuctionEndGroup = new HashMap[Int, String]
    openAuctionEndGroup.put(1, "10:00:00.000")  // +- randomTime
    openAuctionEndGroup.put(2, "10:02:15.000")  // +- randomTime
    openAuctionEndGroup.put(3, "10:04:30.000")  // +- randomTime
    openAuctionEndGroup.put(4, "10:06:45.000")  // +- randomTime
    openAuctionEndGroup.put(5, "10:09:00.000")  // +- randomTime

    val security2group = new HashMap[Char, Int]
    security2group.put('A', 1)
    security2group.put('B', 1)
    security2group.put('C', 2)
    security2group.put('D', 2)
    security2group.put('E', 2)
    security2group.put('F', 2)
    security2group.put('G', 3)
    security2group.put('H', 3)
    security2group.put('I', 3)
    security2group.put('J', 3)
    security2group.put('K', 3)
    security2group.put('L', 3)
    security2group.put('M', 3)
    security2group.put('N', 4)
    security2group.put('O', 4)
    security2group.put('P', 4)
    security2group.put('Q', 4)
    security2group.put('R', 4)
    security2group.put('S', 5)
    security2group.put('T', 5)
    security2group.put('U', 5)
    security2group.put('V', 5)
    security2group.put('W', 5)
    security2group.put('X', 5)
    security2group.put('Y', 5)
    security2group.put('Z', 5)

    openAuctionEndGroup(security2group(security(1)))

  }

}
