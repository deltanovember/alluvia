package com.alluvia.markets

import com.alluvia.tools.PDFReader
import collection.mutable.HashMap
import io.Source
import java.io.File
import java.nio.charset.MalformedInputException

/**
 * ASX market
 */
trait ASX extends Market {

  val shortable = new HashMap[String, Boolean]

    // Load ISINs
  val isinMap = new HashMap[String, String]

  try {
    for (line <- Source.fromInputStream(getClass.getResourceAsStream("/isin" + getMarketName + ".csv")).getLines) {
      val allTokens = line.split(",", -1)
      val validTokens = line.split(",")

      if ("" != allTokens(3)) {
        val ISIN = allTokens(3)
        val security = allTokens(0) + "." + getSecurityExchange
        isinMap.put(security, ISIN)
      }

    }
  }
  catch {
    case ex: MalformedInputException => ex.printStackTrace()
  }

  val reader = new PDFReader
  val lines = reader.readPDF(new File(getClass.getResource("/shortable" + getMarketName + ".pdf").toURI))
  lines.filter(line => line.length() > 6 && line.contains("AX")).foreach(line => shortable.put(line.substring(line.length() - 7, line.length() - 4), true))

  override def getCloseTime = "16:00:00.000"
  override def getCurrency = "AUD"
  override def getEndAuctionClose = "16:09:25.000"
  override def getIGCurrencySymbol = "A$"
  override def getISIN(security: String) = isinMap(security)
  override def getMarketName = "ASX"

   override def getMinTickSize(price: Double, security: String = ""): Double = {
     /**
    Price Range	Minimum Tick Size
    0.1 cents - 9.9 cents	$0.001
    10 cents - 199.5 cents	$0.005
    $2.00 - $99,999,990	$0.01
    */
      return price match {
        case x if x < 0.099 => 0.001
        case x if x >= 0.1 && x <= 1.995 => 0.005
        case _ => 0.01
      }
   }
  override def getOpenTime = "10:00:00.000"
  override def getCurrencyMultiplier = 100
  override def getSecurityExchange = "AX"
  override def getTimeZone = java.util.TimeZone.getTimeZone("GMT+10")
  override def getUncrossTime = "16:12:15.000"
  override def isShortable(security: String) = shortable.contains(security)

}