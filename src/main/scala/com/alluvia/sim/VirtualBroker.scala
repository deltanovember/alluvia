package com.alluvia.sim

import com.alluvia.markets.Market
import com.alluvia.database.BackTestingLib

import com.alluvialtrading.algo.Position
import com.alluvialtrading.data.Quote
import com.alluvialtrading.data.Trade
import com.alluvialtrading.tools.TraderLib

import java.util.Date
import java.util.Hashtable

import scala.math.abs

/**
 * This class acts as a virtual broker.  It should perform the same functionality as a prime broker.  
 * This means it accepts a list of trades, accepts or rejects based on certain criteria and exits 
 * based on specified criteria.  The input is a CSV file with the columns defined as follows
 * <ol>
 * <li>Market name e.g. ASX</li>
 * <li>Trade date and time e.g. 2010-07-08 16:10:51.797</li>
 * <li>Security name e.g. BHP</li>
 * <li>Trade volume e.g. 4000, -1550. Buy or sell is implied by sign</li>
 * <li>Price e.g. 40.2</li>
 * <li>Exit date/time. If this a number e.g. 60, the broker will interpret this as the number 
 * of seconds from opening. If this is a date such as 2010-04-04 the broker will attempt to exit 
 * on that date.  This must be specified in conjunction with one of the following exit 
 * strategies: OPEN, CLOSE</li>
 * <li>Exit strategy e.g.  Trade.EXIT_AGGRESSIVE, Trade.EXIT_CLOSE, Trade.c, Trade.EXIT_MID,	
 * Trade.EXIT_PASSIVE, Trade.EXIT_VWAP, Trade.EXIT_OPEN</li>
 * </ol>
 * Certain exit strategies require only a date and certain others require both date and time.  
 * The following strategies only require a date: EXIT_CLOSE, EXIT_CLOSE, EXIT_OPEN.  The following
 *  strategies require both date and time: EXIT_MID, EXIT_PASSIVE, EXIT_AGGRESSIVE, EXIT_VWAP.
 * @author dnguyen
 *
 */

object VirtualBroker {
  def main(args: Array[String]) {
    class MyLSE extends com.alluvia.markets.LSE
    new VirtualBroker("brokertrades.csv", new MyLSE)
  }
}

class VirtualBroker(tradeData: String, market: Market) {
  val importDir: String = "import"
  val connector = new BackTestingLib
  val MANAGE_BANK: Boolean = false
  val lib: TraderLib = new TraderLib

  val tradingRecord: StringBuffer = new StringBuffer("TradeNo,Security,DateTime,Price,Volume,Brokerage,ReturnOnRisk,Profit,Cash,Shares,Assets\r\n")
  val startingCash: Double = 100000 * market.getCurrencyMultiplier
  val openPositions: Hashtable[String, Position] = new Hashtable[String, Position]

  val allTrades = importTrades(tradeData)
  val sortedTrades = allTrades.sortBy(x => x.getDate)
  val record = sortedTrades.scanLeft(new ProfitLine(cash = startingCash)) {
    case (cumulative, trade) =>

      val totalTrades = cumulative.tradeNumber + 1
      val stock = trade.getSecurity
      val date = trade.getDate
      val price = if (trade.getPrice <= 0) connector.getMarketPrice(market.getMarketName, trade) else trade.getPrice
      val volume = trade.getVolume
      val brokerage = getBrokerage(trade)

      val current: Position = getPosition(trade.getSecurity)
      val currentProfit = if (null == trade || null == current) 0 else -(current.getPrice * -volume + price * volume)
      val profit = if (null == trade) -brokerage else -brokerage + currentProfit
      val returnOnRisk = 100 * profit / (price * volume)

      storePosition(trade, volume, price)
      val shares = lib.calculatePosition(openPositions)
      val cash = cumulative.cash + profit
      val currentCash: Double = cash - shares

      val assets = currentCash + shares
      val profitLine = new ProfitLine(totalTrades, stock, date, price, volume, brokerage, returnOnRisk, profit, cash, shares, assets)

      (profitLine)
  }
  for (single <- record.tail) {
    tradingRecord.append(single.toString() + "\r\n")
  }
  lib.writeFile(".", "profit" + tradeData, tradingRecord.toString)

  def getAllTrades(rawData: List[String]): List[Trade] = {

    if (Nil == rawData) return List()
    if (rawData.length == 0) List()
    val line = rawData(0)

    val parse = line.contains(",")
    if (parse) {
      val splitLine: Array[String] = line.split(",")
      val market: String = splitLine(0)
      val date: Date = lib.convertISODateTimeString(splitLine(1))
      val security: String = splitLine(2)
      val volume: Int = Integer.parseInt(splitLine(3))
      val price: Double = (splitLine(4)).toDouble
      val closeWait: String = splitLine(5)
      val closeStrategy: String = splitLine(6)
      val trade: Trade = new Trade(market, date, security, volume, price, closeWait, closeStrategy)
      if (rawData.length == 1) {
        List(trade)
      }
      else {
        trade :: getAllTrades(rawData.tail)
      }
    }
    else
      getAllTrades(rawData.tail)

  }

  private def getBrokerage(trade: Trade): Double = {
    val value: Double = abs(trade.getPrice * trade.getVolume)
    if (getBrokerageRate * value > getBrokerageMin) {
      getBrokerageRate * value
    }
    else {
      getBrokerageMin
    }
  }

  def getBrokerageRate: Double = {
    0.001
  }

  def getBrokerageMin: Double = {
    10
  }

  /**
   * Get position in particular stock
   * @param security
   * @return
   */
  private def getPosition(security: String): Position = {
    openPositions.get(security)
  }

  private def importTrades(tradeData: String): List[Trade] = {
    if (tradeData.charAt(0) == '.') {
      return List()
    }
    val rawData: Array[String] = lib.openFile(importDir, tradeData)
    val start: Int = if (rawData(0).contains("arket")) 1 else 0
    val allTrades = getAllTrades(rawData.toList)

    injectClosingTrades(allTrades)

  }

  def injectClosingTrades(allTrades: List[Trade]): List[Trade] = {

    if (allTrades.length == 0) return List()
    val trade = allTrades.head
    if (trade.getSecurity == "BNKR.L") {
      println("debug")
    }

    val close: Trade = new Trade(trade.getMarket, new Date, trade.getSecurity, -trade.getVolume, 0, "0", trade.getClosingStrategy)

    if (trade.getClosingStrategy == Trade.EXIT_CLOSE) {
         val nextClose = connector.getClosingTrade(market.getMarketName, trade.getSecurity,
           connector.getTradingDate(market.getMarketName, lib.dateToISODateString(trade.getDate), 1), "")
      close.setPrice(nextClose.getPrice)
      close.setClosingTrade(true)
      close.setDate(nextClose.getDate)
           trade :: close :: injectClosingTrades(allTrades.tail)
    }
    else if (trade.getCloseWait.length >= 0) {
      val newDate: Date =
        if (trade.getClosingStrategy == Trade.EXIT_VWAP) {
          new Date(1000 * Integer.parseInt(trade.getCloseWait) + trade.getDate.getTime)
        }
        else if (trade.getCloseWait.contains("-")) {
          lib.convertISODateTimeString(trade.getCloseWait)
        }
        else {
          new Date(1000 * Integer.parseInt(trade.getCloseWait) + trade.getDate.getTime)
        }

      val exitPrice: Double =
        if (trade.getClosingStrategy == Trade.EXIT_VWAP) {
          connector.getVWAPPrice(market.getMarketName, close)
        }
        else if (trade.getClosingStrategy == Trade.EXIT_LAST_TRADE) {
          val tradeTime: String = lib.dateToTimeString(trade.getDate)
          val dateString: String = lib.dateToISODateString(trade.getDate)
          if (tradeTime.compareTo(market.getCloseTime) > 0) {
            val exitDay: String = {
              val result = connector.getTradingDate(market.getMarketName, dateString, 1)
              if (result.contains("-")) trade.getCloseWait else result
            }

            val lastTrade: Trade = connector.getLastTrade(market.getMarketName, trade.getSecurity, exitDay)
            if (null == lastTrade) {
              trade.setClosingStrategy(Trade.EXIT_AGGRESSIVE)
              val lastQuote: Quote = connector.getQuote(market.getMarketName, trade.getSecurity, exitDay, market.getCloseTime)
              if (null != lastQuote) {
                close.setDate(lastQuote.getDateTime)
                connector.getMarketPrice(market.getMarketName, trade)

              }
              else {
                close.setDate(trade.getDate)
                trade.getPrice

              }
            }
            else {
              close.setDate(lastTrade.getDate)
              lastTrade.getPrice
            }
          }
          else {
            connector.getClosePrice(market.getMarketName, trade.getSecurity, lib.convertISODateTimeString(dateString))
          }
        }
        else {
          connector.getMarketPrice(market.getMarketName, close)
        }
      close.setPrice(exitPrice)
      close.setClosingTrade(true)
      trade :: close :: injectClosingTrades(allTrades.tail)
    }
    else injectClosingTrades(allTrades.tail)

  }

  /**
   * Store the net position in each stock for bankroll management
   * @param info
   * @param type
   */
  private def storePosition(trade: Trade, volume: Int, price: Double) {
    val stockPosition: Double = if (volume < 0) -1 * volume * price else volume * price

    val currentPosition: Position = openPositions.get(trade.getSecurity)
    if (null == currentPosition) {
      openPositions.put(trade.getSecurity, new Position(trade, volume, price))
    }
    else {
      currentPosition.addToPosition(volume, price)
      if (0 == currentPosition.getVolume) {
        openPositions.remove(trade.getSecurity)
      }
    }
  }

}

class ProfitLine(
                  val tradeNumber: Int = 0,
                  val security: String = "",
                  val dateTime: Date = new Date,
                  val price: Double = 0,
                  val volume: Double = 0,
                  val brokerage: Double = 0,
                  val returnOnRisk: Double = 0,
                  val profit: Double = 0,
                  val cash: Double = 0,
                  val shares: Double = 0,
                  val assets: Double = 0
                  ) {

  override def toString() = {
    tradeNumber + "," +
      security + "," +
      dateTime + "," +
      price + "," +
      volume + "," +
      brokerage + "," +
      returnOnRisk + "," +
      profit + "," +
      (cash - shares) + "," +
      shares + "," +
      assets
  }
}

