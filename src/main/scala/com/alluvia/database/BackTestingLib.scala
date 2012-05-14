package com.alluvia.database

//import com.alluvial.mds.contract.Trade

import com.alluvialtrading.data.Benchmark
import com.alluvialtrading.data.Quote
import com.alluvialtrading.data.Trade
import com.alluvialtrading.tools.TraderLib

import com.alluvia.types.benchmark._

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

import scala.math.abs
import com.alluvia.types.market._
import java.util.Date
import com.alluvial.mds.contract.QuoteMatch
import com.alluvia.types.ObservedEventPump

class BackTestingLib extends DatabaseConnector {

  protected val lib = new TraderLib
  val LSE_PRICE_FORMAT: String = "LSE_PRICE_FORMAT"
  val LSE_PRICE_THRESHOLDS: String = "LSE_PRICE_THRESHOLDS"
  val TRADING_DAYS: String = "TRADING_DAYS"
  val fetchNumber = 5000
  val fetchQuery = "FETCH FORWARD " + fetchNumber + " FROM myCursor"

  def getAllEvents(marketName: String, eventPump: ObservedEventPump, startDate: Date,
                   endDate: Date, historical: Boolean = false): Unit = {

    val query: String = "SELECT * " + " FROM \"" + marketName + "RAW" + "\" WHERE \"DATETIME\">='" +
      startDate + "' and \"DATETIME\"<='" + lib.dateToISODateTimeString(endDate) + "' ORDER BY \"DATETIME\", \"TRANSID\""
    System.out.println(query)
    println(new Date)
    try {
      connection.setAutoCommit(false)
      statement = connection.createStatement

      val cursor = "BEGIN WORK; DECLARE myCursor SCROLL CURSOR FOR " + query
      statement.executeUpdate(cursor)

      val result = statement.executeQuery(fetchQuery)

      eventPump.addEvent(new Start)
      if (historical) {
        pumpBenchmarkEvents(result, eventPump)
      }
      else
        pumpEvents(result, eventPump)
      eventPump.addEvent(new End)
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(query)
      }
    }
    println(new Date)
  }

  /**
   * Get all securities that traded on a given date. Uses
   * raw trade table not benchmark table. Therefore slow
   * @return Array of String for all securities trading on that day
   */
  def getAllSecurities(marketName: String, date: String): Array[String] = {
    val startDate: String = date + " 00:00:00"
    val endDate: String = date + " 24:00.00"
    val query: String = "SELECT DISTINCT(\"SECURITY\") FROM \"" + marketName + "TRADES\" " + "WHERE \"DATETIME\" > '" + startDate + "' AND " + "\"DATETIME\" < '" + endDate + "' ORDER BY \"SECURITY\""
    var allSecurities: List[String] = List()
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(query)
      while (result.next) {
        allSecurities = result.getString(1).trim() :: allSecurities
      }
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
      }
    }
    val allArray: Array[String] = allSecurities.toArray
    allArray
  }

  /**
   * Get all securities that traded on a given date
   * @param date Date in ISO format
   * @return
   */
  def getAllTradedSecurities(marketName: String, date: String): Array[String] = {
    val query: String = "SELECT \"SECURITY\" FROM \"" + marketName + "BENCHMARK\" " + "WHERE \"DATE\" = '" + date + "'"
    var allSecurities: List[String] = List()
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(query)

      while (result.next) {
        allSecurities = result.getString(1).trim :: allSecurities
      }
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
      }
    }

    allSecurities.toArray
  }


  private def getBenchmarkTable(marketName: String): String = {
    marketName + "BENCHMARK"
  }

  def getBenchmarkData(marketName: String, security: String, date: String): Benchmark = {
    val getTcountQuery: String = "SELECT *" + " FROM \"" + getBenchmarkTable(marketName) + "\" where \"SECURITY\"='" + security + "' and \"DATE\"='" + date + "'"
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(getTcountQuery)
      if (result.next) {
        val open: Double = result.getDouble(3)
        val close: Double = result.getDouble(4)
        val preclose: Double = result.getDouble(5)
        val high: Double = result.getDouble(6)
        val low: Double = result.getDouble(7)
        val tcount: Int = result.getInt(8)
        val volume: Int = result.getInt(9)
        val value: Double = result.getDouble(10)
        return new Benchmark(security, date, open, close, preclose, high, low, tcount, volume, value)
      }
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(getTcountQuery)
      }
    }
    null
  }

  /**
   * Get the market close price on the date specified
   * @param date
   * @param security
   * @return
   */
  def getClosePrice(marketName: String, security: String, date: Date): Double = {
    val startString: String = lib.dateToISODateTimeString(lib.getDateStart(date))
    val endString: String = lib.dateToISODateTimeString(lib.getDateEnd(date))
    val query: String = "SELECT \"TRADEPRICE\"" + " FROM \"" + getTradeTable(marketName) + "\" where \"DATETIME\">='" + startString + "' and \"DATETIME\"<='" + endString + "' and \"SECURITY\"='" + security + "'" + "  ORDER BY \"DATETIME\" DESC LIMIT 1"
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(query)
      if (result.next) {
        return round4(result.getDouble(1))
      }
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(query)
      }
    }
    -1
  }

  /**
   *
   * @param security
   * @param date
   * @return
   */
  def getClosingTrade(marketName: String, security: String, dateStart: String, dateEnd: String = ""): Trade = {

    var trade: Trade = null
    val newDateEnd = if (dateEnd == "") lib.dateToISODateTimeString(lib.combineDateTime(dateStart, "23:59:59")) else dateEnd
    var query: String = "SELECT \"TRADEPRICE\", \"TRADEVOLUME\", \"DATETIME\"" + " FROM \"" + getTradeTable(marketName) + "\" where \"DATETIME\"<='" + newDateEnd + "' and \"DATETIME\">='" + dateStart + "' and \"SECURITY\"='" + security + "'" + "  ORDER BY \"DATETIME\" DESC LIMIT 1"
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(query)
      if (result.next) {
        trade = new Trade
        trade.setPrice(round4(result.getDouble(1)))
        trade.setDate(result.getTimestamp(3))
      }
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(query)
      }
    }
    if (null != trade) {
      query = "SELECT sum(\"TRADEVOLUME\")" + " FROM \"" + getTradeTable(marketName) + "\" where \"DATETIME\"<='" + newDateEnd + "' and \"DATETIME\">='" + dateStart + "' and \"SECURITY\"='" + security + "'"
      try {
        statement = connection.createStatement
        val result: ResultSet = statement.executeQuery(query)
        if (result.next) {
          trade.setVolume(result.getInt(1))
        }
        statement.close()
      }
      catch {
        case ex: SQLException => {
          System.err.println("SQLException: " + ex.getMessage)
          System.out.println(query)
        }
      }
    }
    trade
  }


  /**
   * Get the first price between two dates
   * @param security    security to look up
   * @param dateStart  startOfDay date
   * @param dateEnd
   * @return
   */
  def getFirstPrice(marketName: String, security: String, dateStart: Date, dateEnd: Date): Double = {
    val startString: String = lib.dateToISODateTimeString(dateStart)
    val endString: String = lib.dateToISODateTimeString(dateEnd)
    val query: String = "SELECT \"TRADEPRICE\"" + " FROM \"" + getTradeTable(marketName) + "\" where \"DATETIME\">='" + startString + "' and \"DATETIME\"<='" + endString + "' and \"SECURITY\"='" + security + "'" + "  ORDER BY \"DATETIME\" ASC LIMIT 1"
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(query)
      if (result.next) {
        return round4(result.getDouble(1))
      }
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(query)
      }
    }
    -1
  }

  /**
   * Get the date of first trade in database
   * @return
   */
  def getFirstTradeDate: Date = {
    val query: String = "SELECT MIN(\"DATETIME\") FROM \"ASXTRADES\""
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(query)
      if (result.next) {
        return result.getTimestamp(1)
      }
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
      }
    }
    null
  }

  def getLastTransactionDate(marketName: String, latest: java.util.Date): java.util.Date = {

    val query: String = "SELECT * " + " FROM \"" + marketName + "RAW" + "\" WHERE" +
      " \"DATETIME\" < '" + latest + "' ORDER BY \"DATETIME\" DESC LIMIT 1"
    val date: java.util.Date = try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(query)
      val latest = if (result.next) {
        result.getTimestamp("DATETIME")
      }
      else {
        new java.util.Date()
      }
      statement.close()
      latest
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(query)
      }
      new java.util.Date()
    }
    date
  }

  /**
   * Get the last price between two dates
   * @param security
   * @param dateStart startOfDay date
   * @param dateEnd   endOfDay date
   * @return
   */
  def getLastPrice(marketName: String, security: String, dateStart: Date, dateEnd: Date): Double = {
    val startString: String = lib.dateToISODateTimeString(dateStart)
    val endString: String = lib.dateToISODateTimeString(dateEnd)
    val query: String = "SELECT \"TRADEPRICE\"" + " FROM \"" + getTradeTable(marketName) + "\" where \"DATETIME\">='" + startString + "' and \"DATETIME\"<='" + endString + "' and \"SECURITY\"='" + security + "'" + "  ORDER BY \"DATETIME\" DESC LIMIT 1"
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(query)
      if (result.next) {
        return round4(result.getDouble(1))
      }
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(query)
      }
    }
    -1
  }

  /**
   *
   * @param segment
   * @param currency
   * @param price
   * @return tick size if found, -1
   */
  def getLSETickSize(segment: String, currency: String, price: Double): Double = {
    val tickSize: Double = -1
    val query: String = "SELECT \"VALUE\"" + " FROM \"" + LSE_PRICE_FORMAT + "\" where \"CODE\"=" + "(SELECT \"CODE\"" + " FROM \"" + LSE_PRICE_THRESHOLDS + "\" where \"SEGMENT\"='" + segment + "' and \"CURRENCY\"='" + currency + "' and \"MINPRICE\"<" + price + " and \"MAXPRICE\">" + price + " LIMIT 1)"
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(query)
      if (result.next) {
        return result.getDouble(1)
      }
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(query)
      }
    }
    tickSize
  }

  /**
   *
   * @param security
   * @param date
   * @return
   */
  def getLastTrade(marketName: String, security: String, date: String): Trade = {
    val dateStart: String = date
    val dateEnd: String = date + " 23:59:00"
    var trade: Trade = null
    var query: String = "SELECT \"TRADEPRICE\", \"TRADEVOLUME\", \"DATETIME\"" + " FROM \"" + getTradeTable(marketName) + "\" where \"DATETIME\"<='" + dateEnd + "' and \"DATETIME\">='" + dateStart + "' and \"SECURITY\"='" + security + "'" + "  ORDER BY \"DATETIME\" DESC LIMIT 1"
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(query)
      if (result.next) {
        trade = new Trade
        trade.setPrice(round4(result.getDouble(1)))
        trade.setDate(result.getTimestamp(3))
      }
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(query)
      }
    }
    if (null != trade) {
      query = "SELECT sum(\"TRADEVOLUME\")" + " FROM \"" + getTradeTable(marketName) + "\" where \"DATETIME\"<='" + dateEnd + "' and \"DATETIME\">='" + dateStart + "' and \"SECURITY\"='" + security + "'"
      try {
        statement = connection.createStatement
        val result: ResultSet = statement.executeQuery(query)
        if (result.next) {
          trade.setVolume(result.getInt(1))
        }
        statement.close()
      }
      catch {
        case ex: SQLException => {
          System.err.println("SQLException: " + ex.getMessage)
          System.out.println(query)
        }
      }
    }
    trade
  }

  /**
   * Get the date of last trade in database
   * @return
   */
  def getLastTradeDate: Date = {
    val query: String = "SELECT MAX(\"DATETIME\") FROM \"ASXTRADES\""
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(query)
      if (result.next) {
        return result.getTimestamp(1)
      }
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
      }
    }
    null
  }

  def getMarketPrice(marketName: String, trade: Trade): Double = {
    val query: String = "SELECT \"BID\",\"ASK\"" + " FROM \"" + getQuoteTable(marketName) + "\" where \"DATETIME\"<='" + lib.dateToISODateTimeString(trade.getDate) + "' and \"SECURITY\"='" + trade.getSecurity + "'  ORDER BY \"DATETIME\" DESC LIMIT 1"
    var closeStrategy: String = trade.getClosingStrategy
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(query)
      if (result.next) {
        val bid: Double = result.getDouble(1)
        val ask: Double = result.getDouble(2)
        val mid: Double = (bid + ask) / 2
        if (closeStrategy == Trade.EXIT_MID) {
          return mid
        }
        else if (closeStrategy == Trade.EXIT_AGGRESSIVE) {
          if (trade.getVolume > 0) {
            return ask
          }
          else {
            return bid
          }
        }
        else {
          if (trade.getVolume > 0) {
            return bid
          }
          else {
            return ask
          }
        }
      }
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(query)
      }
    }
    0
  }

  def getMaxPrice(marketName: String, security: String, dateEnd: Date, maxEvents: Int): Double = {
    val endString: String = lib.dateToISODateTimeString(dateEnd)
    val query: String = "SELECT MAX(\"TRADEPRICE\") " + " FROM (SELECT \"TRADEPRICE\" FROM \"" + marketName + "TRADES\" " + " WHERE \"DATETIME\" <='" + endString + "' and \"SECURITY\"='" + security + "'  " + " ORDER BY \"DATETIME\" DESC LIMIT " + maxEvents + ") AS PRICEQUERY"
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(query)
      if (result.next) {
        return round4(result.getDouble(1))
      }
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(query)
      }
    }
    -1
  }

  /**
   * Get the maximum trade price between two dates
   * @param security, String
   * @param dateEnd, Date object
   * @param dateStart, Date object
   * @return price, double
   */
  def getMaxPrice(marketName: String, security: String, dateStart: Date, dateEnd: Date): Double = {
    val startString: String = lib.dateToISODateTimeString(dateStart)
    val endString: String = lib.dateToISODateTimeString(dateEnd)
    val query: String = "SELECT \"TRADEPRICE\"" + " FROM \"" + getTradeTable(marketName) + "\" where \"DATETIME\">='" + startString + "' and \"DATETIME\"<='" + endString + "' and \"SECURITY\"='" + security + "'" + "  ORDER BY \"TRADEPRICE\" DESC LIMIT 1"
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(query)
      if (result.next) {
        return round4(result.getDouble(1))
      }
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(query)
      }
    }
    -1
  }

  def getMinPrice(marketName: String, security: String, dateEnd: Date, maxEvents: Int): Double = {
    val endString: String = lib.dateToISODateTimeString(dateEnd)
    val query: String = "SELECT MIN(\"TRADEPRICE\") " + " FROM (SELECT \"TRADEPRICE\" FROM \"" + marketName + "TRADES\" " + " WHERE \"DATETIME\" <='" + endString + "' and \"SECURITY\"='" + security + "'  " + " ORDER BY \"DATETIME\" DESC LIMIT " + maxEvents + ") AS PRICEQUERY"
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(query)
      if (result.next) {
        return round4(result.getDouble(1))
      }
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(query)
      }
    }
    -1
  }

  /**
   * Get the minimum trade price between two dates
   * @param security, String
   * @param dateEnd, Date object
   * @param dateStart, Date object
   * @return price, double
   */
  def getMinPrice(marketName: String, security: String, dateStart: Date, dateEnd: Date): Double = {
    val startString: String = lib.dateToISODateTimeString(dateStart)
    val endString: String = lib.dateToISODateTimeString(dateEnd)
    val query: String = "SELECT \"TRADEPRICE\"" + " FROM \"" + getTradeTable(marketName) + "\" where \"DATETIME\">='" + startString + "' and \"DATETIME\"<='" + endString + "' and \"SECURITY\"='" + security + "'" + "  ORDER BY \"TRADEPRICE\" ASC LIMIT 1"
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(query)
      if (result.next) {
        return round4(result.getDouble(1))
      }
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(query)
      }
    }
    -1
  }

  def getMinSpread(marketName: String, security: String, dateStart: String, dateEnd: String, useBenchmark: Boolean): Double = {
    val startString: String = dateStart
    val endString: String = dateEnd + " 23:59:59"
    var query: String = "SELECT \"DATETIME\", \"BID\", \"ASK\", MIN(\"ASK\" - \"BID\") AS DELTA " + " FROM \"" + getQuoteTable(marketName) + "\" WHERE " + " \"SECURITY\"='" + security + "' AND \"DATETIME\">'" + startString + "' AND " + " \"DATETIME\"<'" + endString + "' " + " GROUP BY \"DATETIME\", \"BID\", \"ASK\" " + " HAVING (\"ASK\" - \"BID\") > 0 " + " ORDER BY MIN(\"ASK\" - \"BID\") LIMIT 1"
    if (useBenchmark) {
      query = "SELECT \"MINSPREAD\"" + " FROM \"" + getBenchmarkTable(marketName) + "\" WHERE " + " \"SECURITY\"='" + security + "' AND " + " \"DATE\"='" + dateStart + "'"
    }
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(query)
      if (result.next) {
        if (useBenchmark) {
          return round4(result.getDouble(1))
        }
        else return round4(result.getDouble(4))
      }
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(query)
      }
    }
    -1
  }

  /**
   * Get first trade of the day
   * @param security
   * @param date
   * @return
   */
  def getOpeningTrade(marketName: String, security: String, date: String): Trade = {
    val dateStart: String = date + " 07:30:00"
    val dateEnd: String = date + " 10:11:00"
    var trade: Trade = null
    var query: String = "SELECT \"TRADEPRICE\", \"TRADEVOLUME\", \"DATETIME\"" + " FROM \"" + getTradeTable(marketName) + "\" where \"DATETIME\"<='" + dateEnd + "' and \"DATETIME\">='" + dateStart + "' and \"SECURITY\"='" + security + "'" + "  ORDER BY \"DATETIME\" ASC LIMIT 1"
    System.out.println(query)
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(query)
      if (result.next) {
        trade = new Trade
        trade.setPrice(round4(result.getDouble(1)))
        trade.setDate(result.getTimestamp(3))
      }
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(query)
      }
    }
    if (null != trade) {
      query = "SELECT sum(\"TRADEVOLUME\")" + " FROM \"" + getTradeTable(marketName) + "\" where \"DATETIME\"='" + lib.dateToISODateTimeString(trade.getDate) + "' and \"SECURITY\"='" + security + "'"
      try {
        statement = connection.createStatement
        val result: ResultSet = statement.executeQuery(query)
        if (result.next) {
          trade.setVolume(result.getInt(1))
        }
        statement.close()
      }
      catch {
        case ex: SQLException => {
          System.err.println("SQLException: " + ex.getMessage)
          System.out.println(query)
        }
      }
    }
    trade
  }

  /**
   * Get the market open price on the date specified
   * @param date
   * @param security
   * @return
   */
  def getOpenPrice(marketName: String, security: String, date: Date): Double = {
    val startString: String = lib.dateToISODateTimeString(lib.getDateStart(date))
    val endString: String = lib.dateToISODateTimeString(lib.getDateEnd(date))
    val query: String = "SELECT \"TRADEPRICE\"" + " FROM \"" + getTradeTable(marketName) + "\" where \"DATETIME\">='" + startString + "' and \"DATETIME\"<='" + endString + "' and \"SECURITY\"='" + security + "'" + "  ORDER BY \"DATETIME\" ASC LIMIT 1"
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(query)
      if (result.next) {
        return round4(result.getDouble(1))
      }
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(query)
      }
    }
    -1
  }

  /**
   *
   * @param date
   * @param time
   * @param security
   * @return
   */
  def getQuote(marketName: String, security: String, date: String, time: String): Quote = {
    val startTime: String = date
    val endTime: String = date + " " + time
    val query: String = "SELECT \"BID\", \"ASK\", \"BIDVOLUME\", \"ASKVOLUME\", \"DATETIME\"" + " FROM \"" + getQuoteTable(marketName) + "\" where \"DATETIME\">='" + startTime + "' and \"DATETIME\"<='" + endTime + "' and \"SECURITY\"='" + security + "'  ORDER BY \"DATETIME\" DESC LIMIT 1"
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(query)
      if (result.next) {
        var bidVolume: Int = result.getInt(3)
        var askVolume: Int = result.getInt(4)
        val bid: Double = result.getDouble(1)
        val ask: Double = result.getDouble(2)
        val dateTime: String = result.getString(5)
        val quote: Quote = new Quote(lib.convertISODateTimeString(dateTime), security, bidVolume, askVolume, bid, ask)
        return quote
      }
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(query)
      }
    }
    null
  }

  /**
   *
   * @param date
   * @param time
   * @param security
   * @return
   */
  def getTrade(marketName: String, date: String, time: String, security: String): Trade = {
    var startTime: String = date
    var endTime: String = date + " " + time
    var trade: Trade = null
    var query: String = "SELECT \"TRADEPRICE\", \"TRADEVOLUME\", \"DATETIME\"" + " FROM \"" + getTradeTable(marketName) + "\" where \"DATETIME\">='" + startTime + "' and \"DATETIME\"<='" + endTime + "' and \"SECURITY\"='" + security + "'" + "  ORDER BY \"DATETIME\" DESC LIMIT 1"
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(query)
      if (result.next) {
        trade = new Trade
        trade.setPrice(round4(result.getDouble(1)))
        trade.setDate(result.getTimestamp(3))
      }
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(query)
      }
    }
    if (null != trade) {
      query = "SELECT sum(\"TRADEVOLUME\")" + " FROM \"" + getTradeTable(marketName) + "\" where \"DATETIME\">='" + startTime + "' and \"DATETIME\"<='" + endTime + "' and \"SECURITY\"='" + security + "'"
      try {
        statement = connection.createStatement
        val result: ResultSet = statement.executeQuery(query)
        if (result.next) {
          trade.setVolume(result.getInt(1))
        }
        statement.close()
      }
      catch {
        case ex: SQLException => {
          System.err.println("SQLException: " + ex.getMessage)
          System.out.println(query)
        }
      }
    }
    trade
  }

  /**
   * Get a particular trading date
   * @param date
   * @param offset 2=2 days ahead, -5=5 days back
   * @return
   */
  def getTradingDate(marketName: String, date: String, offset: Int): String = {
    var sign: String = null
    if (0 == offset) {
      return date
    }
    else if (offset > 0) {
      sign = ">"
    }
    else {
      sign = "<"
    }
    var query: String = "SELECT \"DATE\"" + " FROM \"" + TRADING_DAYS + "\" where \"DATE\"" + ">=" + "'" + date + "' AND \"MARKET\"='" + marketName + "'"
    val scroll: Int = ResultSet.TYPE_SCROLL_INSENSITIVE
    val update: Int = ResultSet.CONCUR_UPDATABLE
    /**
    try {
      preparedStatement = connection.prepareStatement(query, scroll, update)
      val result: ResultSet = preparedStatement.executeQuery
      if (!result.next) {
        return null
      }
    }
    catch {
      case ex: SQLException => {
        ex.printStackTrace()
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(query)
      }
    }
     */
    query = "SELECT \"DATE\"" + " FROM \"" + TRADING_DAYS + "\" where \"DATE\"" + "<=" + "'" + date + "' AND \"MARKET\"='" + marketName + "'"
    try {
      preparedStatement = connection.prepareStatement(query, scroll, update)
      val result: ResultSet = preparedStatement.executeQuery
      if (!result.next) {
        return null
      }
    }
    catch {
      case ex: SQLException => {
        ex.printStackTrace()
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(query)
      }
    }
    query = "SELECT \"DATE\"" + " FROM \"" + TRADING_DAYS + "\" where \"DATE\"" + sign + "'" + date + "' AND \"MARKET\"='" + marketName + "'"
    try {
      preparedStatement = connection.prepareStatement(query, scroll, update)
      val result: ResultSet = preparedStatement.executeQuery
      if (!result.next) {
        return null
      }
      result.absolute(offset)
      return result.getString(1)
    }
    catch {
      case ex: SQLException => {
        ex.printStackTrace()
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(query)
      }
    }
    null
  }

  /**
   * Get transaction count between two dates inclusive
   * @param security
   * @param dateStart
   * @param dateEnd
   * @return
   */
  def getTcount(marketName: String, security: String, dateStart: String, dateEnd: String): Int = {
    val getTcountQuery: String = "SELECT SUM(\"TCOUNT\") " + " FROM \"" + getBenchmarkTable(marketName) + "\" where \"SECURITY\"='" + security + "' and \"DATE\">='" + dateStart + "' and \"DATE\"<='" + dateEnd + "'"
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(getTcountQuery)
      if (result.next) {
        return result.getInt(1)
      }
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(getTcountQuery)
      }
    }
    -1
  }

  private def getQuoteTable(marketName: String): String = {
    marketName + "QUOTES"
  }

  /**
   * Returns the number of on-market trades according to overloaded
   * getTcount function.
   * @param dateStart
   * @param dateEnd
   * @return
   */
  def getTcount(marketName: String, security: String, dateStart: Date, dateEnd: Date): Int = {
    var totalTrades: Int = 0
    var sameDay: Boolean = false
    try {
      val endDateString: String = lib.dateToISODateString(dateEnd)
      val startDateString: String = lib.dateToISODateString(dateStart)
      if (startDateString == endDateString) {
        sameDay = true
      }
      val startFirstDay: String = lib.dateToISODateTimeString(dateStart)
      var endFirstDay: String = lib.dateToISODateTimeString(lib.combineDateTime(dateStart, "23:59:59"))
      if (sameDay) {
        endFirstDay = lib.dateToISODateTimeString(dateEnd)
      }
      var getTcountQuery: String = "SELECT COUNT(\"TRADEVOLUME\") " + " FROM \"" + getTradeTable(marketName) + "\" where \"SECURITY\"='" + security + "' and \"DATETIME\">='" + startFirstDay + "' and \"DATETIME\"<='" + endFirstDay + "'"
      statement = connection.createStatement
      var result: ResultSet = statement.executeQuery(getTcountQuery)
      if (result.next) {
        totalTrades += result.getInt(1)
        if (sameDay) {
          return totalTrades
        }
      }
      var currentDate: String = getTradingDate(marketName, startDateString, 1)
      while (!(currentDate == endDateString)) {
        val benchmark: Benchmark = getBenchmarkData(marketName, security, currentDate)
        if (null != benchmark) {
          totalTrades += benchmark.getTcount
        }
        currentDate = getTradingDate(marketName, currentDate, 1)
      }
      var startLastDay: String = lib.dateToISODateTimeString(lib.combineDateTime(dateEnd, "00:00:00"))
      var endLastDay: String = lib.dateToISODateTimeString(dateEnd)
      getTcountQuery = "SELECT COUNT(\"TRADEVOLUME\") " + " FROM \"" + getTradeTable(marketName) + "\" where \"SECURITY\"='" + security + "' and \"DATETIME\">='" + startLastDay + "' and \"DATETIME\"<='" + endLastDay + "'"
      statement = connection.createStatement
      result = statement.executeQuery(getTcountQuery)
      if (result.next) {
        totalTrades += result.getInt(1)
        return totalTrades
      }
    }
    catch {
      case e: SQLException => {
        e.printStackTrace()
      }
    }
    finally {
      try {
        statement.close()
      }
      catch {
        case e: SQLException => {
          e.printStackTrace()
        }
      }
    }
    -1
  }

  /**
   * Get value between two dates inclusive
   * @param security
   * @param dateStart
   * @param dateEnd
   * @return
   */
  def getValue(marketName: String, security: String, dateStart: String, dateEnd: String): Double = {
    val getValueQuery: String = "SELECT SUM(\"VALUE\") " + " FROM \"" + getBenchmarkTable(marketName) + "\" where \"SECURITY\"='" + security + "' and \"DATE\">='" + dateStart + "' and \"DATE\"<='" + dateEnd + "'"
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(getValueQuery)
      if (result.next) {
        return result.getDouble(1)
      }
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(getValueQuery)
      }
    }
    -1
  }

  private def getTradeTable(marketName: String): String = {
    marketName + "TRADES"
  }

  /**
   * Returns the value of trades according to overloaded
   * getValue function.
   * @param dateStart  startOfDay date
   * @param dateEnd    endOfDay date
   * @return
   */
  def getValue(marketName: String, security: String, dateStart: Date, dateEnd: Date, maxEvents: Int): Double = {
    var totalValue: Double = 0
    var sameDay: Boolean = false
    try {
      var endDateString: String = lib.dateToISODateString(dateEnd)
      var startDateString: String = lib.dateToISODateString(dateStart)
      if (startDateString == endDateString) {
        sameDay = true
      }
      val startFirstDay = lib.dateToISODateTimeString(dateStart)
      var endFirstDay: String = lib.dateToISODateTimeString(lib.combineDateTime(dateStart, "23:59:59"))
      if (sameDay) {
        endFirstDay = lib.dateToISODateTimeString(dateEnd)
      }
      var getValueQuery: String = "SELECT SUM(\"TRADEPRICE\"* \"TRADEVOLUME\")" + " FROM \"" + getTradeTable(marketName) + "\" where \"SECURITY\"='" + security + "' and \"DATETIME\">='" + startFirstDay + "' and \"DATETIME\"<='" + endFirstDay + "'"
      statement = connection.createStatement
      var result: ResultSet = statement.executeQuery(getValueQuery)
      if (result.next) {
        totalValue += result.getDouble(1)
        if (sameDay) {
          return totalValue
        }
      }
      var currentDate: String = getTradingDate(marketName, startDateString, 1)
      while (!(currentDate == endDateString)) {
        var benchmark: Benchmark = getBenchmarkData(marketName, security, currentDate)
        if (null != benchmark) {
          totalValue += benchmark.getValue
        }
        currentDate = getTradingDate(marketName, currentDate, 1)
      }
      val startLastDay: String = lib.dateToISODateTimeString(lib.combineDateTime(dateEnd, "00:00:00"))
      val endLastDay: String = lib.dateToISODateTimeString(dateEnd)
      getValueQuery = "SELECT (\"TRADEPRICE\"* \"TRADEVOLUME\")" + " FROM \"" + getTradeTable(marketName) + "\" where \"SECURITY\"='" + security + "' and \"DATETIME\">='" + startLastDay + "' and \"DATETIME\"<='" + endLastDay + "'"
      statement = connection.createStatement
      result = statement.executeQuery(getValueQuery)
      if (result.next) {
        totalValue += result.getDouble(1)
        return totalValue
      }
    }
    catch {
      case e: SQLException => {
        e.printStackTrace()
      }
    }
    finally {
      try {
        statement.close()
      }
      catch {
        case e: SQLException => {
          e.printStackTrace()
        }
      }
    }
    -1
  }

  /**
   * Returns the volume of on-market trades according to overloaded
   * getVolume function.
   * @param dateStart
   * @param dateEnd
   * @return
   */
  def getVolume(marketName: String, security: String, dateStart: Date, dateEnd: Date, maxEvents: Int): Int = {
    try {
      val startString: String = lib.dateToISODateTimeString(dateStart)
      val endString: String = lib.dateToISODateTimeString(dateEnd)
      val getVolumeQuery: String = "SELECT SUM(\"TRADEVOLUME\") " + " FROM \"" + getTradeTable(marketName) + "\" where \"DATETIME\">='" + startString + "' and \"DATETIME\"<='" + endString + "' and \"SECURITY\"='" + security + "'"
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(getVolumeQuery)
      if (result.next) {
        return result.getInt(1)
      }
    }
    catch {
      case e: SQLException => {
        e.printStackTrace()
      }
    }
    finally {
      try {
        statement.close()
      }
      catch {
        case e: SQLException => {
          e.printStackTrace()
        }
      }
    }
    -1
  }

  /**
   * Calculates VWAP according to overloaded getVWAP function.
   * @param dateStart
   * @param dateEnd
   * @param maxEvents
   * @return
   */
  def getVWAP(marketName: String, security: String, dateStart: Date, dateEnd: Date, maxEvents: Int): Double = {
    val startString: String = lib.dateToISODateTimeString(dateStart)
    val endString: String = lib.dateToISODateTimeString(dateEnd)
    var getVWAPQuery: String = "SELECT \"TRADEPRICE\", \"TRADEVOLUME\"" + " FROM \"" + getTradeTable(marketName) + "\" where \"DATETIME\">='" + startString + "' and \"DATETIME\"<='" + endString + "' and \"SECURITY\"='" + security + "'" + " ORDER BY \"DATETIME\" "
    if (maxEvents > 0) {
      getVWAPQuery += "ASC LIMIT '" + abs(maxEvents) + "'"
    }
    else if (maxEvents < 0) {
      getVWAPQuery += "DESC LIMIT '" + abs(maxEvents) + "'"
    }
    else {
      getVWAPQuery += "DESC"
    }
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(getVWAPQuery)
      var totalVolume: Double = 0
      var totalValue: Double = 0
      while (result.next) {
        val currentPrice: Double = result.getDouble(1)
        var currentVolume: Double = result.getDouble(2)
        var currentValue: Double = currentPrice * currentVolume
        totalVolume += currentVolume
        totalValue += currentValue
      }
      statement.close()
      if (totalVolume == 0)
        return 0
      else
        return round4(totalValue / totalVolume)
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(getVWAPQuery)
      }
    }
    0
  }

  /**
   * Exit strategy assuming we capture VWAP
   * @param trade
   * @return
   */
  def getVWAPPrice(marketName: String, trade: Trade): Double = {
    val query: String = "SELECT \"TRADEPRICE\",\"TRADEVOLUME\"" + " FROM \"" + getTradeTable(marketName) + "\" where \"DATETIME\">='" + lib.dateToISODateTimeString(trade.getDate) + "' and \"SECURITY\"='" + trade.getSecurity + "'" + "  ORDER BY \"DATETIME\" ASC LIMIT 1000"
    System.out.println(query)
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(query)
      val tradeVolume = abs(trade.getVolume)
      var totalVolume: Double = 0
      var totalValue: Double = 0
      while (result.next && totalVolume <= 5 * tradeVolume) {
        val currentPrice: Double = result.getDouble(1)
        var currentVolume: Double = result.getDouble(2)
        var currentValue: Double = currentPrice * currentVolume
        totalVolume += currentVolume
        totalValue += currentValue
      }
      statement.close()
      return totalValue / totalVolume
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(query)
      }
    }
    0
  }

  def insertQuote(marketName: String, quote: Quote) {
    var insertString: String = null
    insertString = "insert into \"" + getQuoteTable(marketName) + "\" values(" + "'" + lib.dateToISODateTimeString(quote.getDateTime) + "'," + "'" + quote.getSecurity + "'," + quote.getBid + "," + quote.getAsk + "," + quote.getBidVolume + "," + quote.getAskVolume + ")"
    try {
      statement = connection.createStatement
      statement.executeUpdate(insertString)
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(insertString)
      }
    }
  }

  def insertBenchmark(marketName: String, date: String, security: String, open: Double, close: Double, preclose: Double, high: Double, low: Double, tcount: Int, volume: Int, value: Double, minSpread: Double): Unit = {
    var insertString: String = null
    insertString = "INSERT into \"" + marketName + "BENCHMARK\" values(" + "'" + date + "'," + "'" + security + "'," + open + "," + close + "," + preclose + "," + high + "," + low + "," + tcount + "," + volume + "," + value + "," + minSpread + ")"
    try {
      statement = connection.createStatement
      statement.executeUpdate(insertString)
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(insertString)
      }
    }
  }

  def isTradingDate(market: String, date: Date): Boolean = {
    val tradingDate = false
    val dateString = lib.dateToISODateString(date)
    val query = "SELECT \"MARKET\",\"DATE\"" + " FROM \"" + TRADING_DAYS + "\" where \"MARKET\"='" + market + "' and \"DATE\"='" + dateString + "'"
    try {
      statement = connection.createStatement
      val result: ResultSet = statement.executeQuery(query)
      if (result.next) {
        return true
      }
      statement.close()
    }
    catch {
      case ex: SQLException => {
        System.err.println("SQLException: " + ex.getMessage)
        System.out.println(query)
      }
    }
    tradingDate
  }

  def pumpBenchmarkEvents(result: ResultSet, eventPump: ObservedEventPump) {

    var hasData = false

    while (result.next) {
      hasData = true
      result.getString("TRANSTYPE") match {
        case "TRADE" =>
          val trade = new BTrade(0, 0, 0, 0, 0, 0, 0.0, result.getInt("VOLUME"), result.getDouble("PRICE"), result.getTimestamp("DATETIME").getTime, 0, "o", result.getTimestamp("DATETIME").getTime, 0)
          trade.Security = processSecurity(result.getString("SECURITY"))
          eventPump.addEvent(trade)
        case _ =>
      }

    }
    if (hasData) {
      pumpBenchmarkEvents(connection.createStatement.executeQuery(fetchQuery), eventPump)
    }
    else {
      println("clo@ng")
      statement.executeUpdate("CLOSE mycursor")
    //  connection.close
    }

  }

  def processSecurity(security: String) = security.split("\\.")(0)

  def pumpEvents(resultSet: ResultSet, observedEventPump: ObservedEventPump) {

    var hasData = false
    pumpSingleBatchOfEvents(resultSet, observedEventPump)

    def pumpSingleBatchOfEvents(result: ResultSet, eventPump: ObservedEventPump) {

      while (result.next) {
        hasData = true
        result.getString("TRANSTYPE") match {
          case "TRADE" =>
            val trade = new com.alluvial.mds.contract.Trade(0, 0, 0, 0, 0, 0, 0.0, result.getInt("VOLUME"), result.getDouble("PRICE"), result.getTimestamp("DATETIME").getTime, 0, "o", result.getTimestamp("DATETIME").getTime, 0)
            trade.Security = processSecurity(result.getString("SECURITY"))
            eventPump.addEvent(trade)

          case "MATCH" =>
            val quoteMatch = new com.alluvial.mds.contract.QuoteMatch(0, result.getInt("VOLUME"), 0, result.getDouble("PRICE"), result.getTimestamp("DATETIME").getTime, 0)
            quoteMatch.Security = processSecurity(result.getString("SECURITY"))
            eventPump.addEvent(quoteMatch)

          case "CONTROL" =>
            val tagfield = result.getString("TAGFIElD")
            if (tagfield == "ENDOFDAY") {
              val endOfDay = new DayEnd(result.getTimestamp("DATETIME"))
              eventPump.addEvent(endOfDay)
            }
            else if (tagfield == "STARTOFDAY") {
              val startOfDay = new DayStart(result.getTimestamp("DATETIME"))
              eventPump.addEvent(startOfDay)
            }
            else {
              println("unknown control")
              System.exit(0)
            }


          case "ENTORD" =>
            val price = result.getDouble("PRICE")
            result.getString("TAGFIElD") match {
              case "B" =>

                val bid = new com.alluvial.mds.contract.Quote(0, 'B', price, 0, result.getInt("VOLUME"), "", result.getTimestamp("DATETIME").getTime, 0)
                bid.Security = processSecurity(result.getString("SECURITY"))
                eventPump.addEvent(bid)

              case "A" =>

                val ask = new com.alluvial.mds.contract.Quote(0, 'A', price, 0, result.getInt("VOLUME"), "", result.getTimestamp("DATETIME").getTime, 0)
                ask.Security = processSecurity(result.getString("SECURITY"))
                eventPump.addEvent(ask)
              case invalid =>
               // val unknown = new com.alluvia.types.Quote(processSecurity(result.getString("SECURITY")), result.getTimestamp("DATETIME"), 'A', price, 1,
                //  result.getInt("VOLUME"), "")
                println("unknown tagfield " + invalid)
                System.exit(0)

            }


          case "INDEX" =>
          case invalid =>
            println("urecognised transtype" + invalid)
            System.exit(0)
        }

      }
    }

    while (hasData) {
      hasData = false
      pumpSingleBatchOfEvents(connection.createStatement.executeQuery(fetchQuery), observedEventPump)
    }

  }

  /**
   * Round numbers
   * @param double
   */

  def round0(unrounded: Double): Double = {
    lib.round0(unrounded)
  }

  def round1(unrounded: Double): Double = {
    lib.round1(unrounded)
  }

  def round2(unrounded: Double): Double = unrounded match {
    case Double.NegativeInfinity => unrounded
    case Double.PositiveInfinity => unrounded
    case x if x.isNaN => unrounded
    case _ => lib.round2(unrounded)
  }

  def round3(unrounded: Double): Double = {
    lib.round3(unrounded)
  }

  def round4(unrounded: Double): Double = unrounded match {
    case Double.NegativeInfinity => unrounded
    case Double.PositiveInfinity => unrounded
    case x if x.isNaN => unrounded
    case _ => lib.round4(unrounded)
  }


}

