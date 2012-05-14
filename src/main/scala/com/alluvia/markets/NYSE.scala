package com.alluvia.markets

trait NYSE extends Market {

  override def getCloseTime = "16:00:00.000"
  override def getCurrency = "USD"
  override def getEndAuctionClose = "16:12:15.000"
  override def getMarketName = "NYSE"
  override def getSecurityExchange = "N"
  override def getTimeZone = java.util.TimeZone.getTimeZone("America/New_York")
  override def getUncrossTime = "16:00:00.000" // check
}