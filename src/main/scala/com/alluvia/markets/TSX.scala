package com.alluvia.markets

/**
 * Toronto
 */
trait TSX extends Market {

  override def getCloseTime = "23:59:59.000"
  override def getCurrency = "CAD"
  override def getEndAuctionClose = "23:59:59.000"
  override def getMarketName = "TSX"
  override def getSecurityExchange = "TX"
   override def getTimeZone = java.util.TimeZone.getTimeZone("America/Toronto")
  override def getUncrossTime = "16:00:00.000"

}