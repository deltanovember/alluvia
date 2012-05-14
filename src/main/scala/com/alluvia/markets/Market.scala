package com.alluvia.markets

import java.math.BigDecimal
import scala.math.ceil
import scala.math.floor

/**
 *
 */
trait Market {

  //  Always set time zone
  java.util.TimeZone.setDefault(getTimeZone)

  /**
   * Broker (e.g. IG markets)  multiplier may be different from exchange multiplier
   */
  def getBrokerCurrencyMultiplier = 1.0
	def getCloseTime: String
  def getCurrency: String
  def getCurrencyMultiplier = 1.0
  def getEndAuctionClose: String
  def getIGCurrencySymbol = "$"
  def getISIN(security: String) = ""
  def getMarketName: String
  def getMinTickSize(price: Double, security: String) = 0.0
  def getOpenTime: String = "9:00:00.00"
  def getTimeZone: java.util.TimeZone
  def getTradeableSecurities(date: java.util.Date): List[String] = List()
  def getUncrossTime: String
  def isShortable(security: String) = true

  def forceRoundDown(price: String, minTick: String): Double = {
    val priceDecimal = (new BigDecimal(price)).multiply(new BigDecimal(0.9999999))
    val tickDecimal = new BigDecimal(minTick)
    val expanded = floor(priceDecimal.divide(tickDecimal).doubleValue())
    tickDecimal.multiply(new BigDecimal(expanded)).doubleValue()
  }

    def forceRoundUp(price: String, minTick: String): Double = {
    val priceDecimal = new BigDecimal(price).multiply(new BigDecimal(1.000001))
    val tickDecimal = new BigDecimal(minTick)
    val expanded = ceil(priceDecimal.divide(tickDecimal).doubleValue())
    return tickDecimal.multiply(new BigDecimal(expanded)).doubleValue()
  }

  // Reuters exchange code
  def getSecurityExchange: String

  def roundDown(price: String, minTick: String): Double = {
    val priceDecimal = new BigDecimal(price)
    val tickDecimal = new BigDecimal(minTick)
    val expanded = floor(priceDecimal.divide(tickDecimal).doubleValue())
    tickDecimal.multiply(new BigDecimal(expanded)).doubleValue()
  }

    def roundUp(price: String, minTick: String): Double = {
    val priceDecimal = new BigDecimal(price)
    val tickDecimal = new BigDecimal(minTick)
    val expanded = ceil(priceDecimal.divide(tickDecimal).doubleValue())
    return tickDecimal.multiply(new BigDecimal(expanded)).doubleValue()
  }

}