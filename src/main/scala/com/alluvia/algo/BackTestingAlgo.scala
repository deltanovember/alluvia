package com.alluvia.algo

import java.util.ArrayList
import java.util.Date


import com.alluvia.sim.VirtualBroker

import com.alluvialtrading.data.Quote
import com.alluvialtrading.data.Regression
import com.alluvialtrading.data.Trade
import com.alluvialtrading.tools.MathLib
/**
 * Baseline class to handle trading, cash management and so on.
 * The overall purpose of the class is to expose ALICE like functionality
 * for algorithm designers to use.  The following principles should be kept in mind:
 * <ol>
 * <li>This class is meant to facilitate algorithm development in an easy, powerful and flexible manner.</li>
 * <li>Easy means that anything written within our framework should be no more complex than the ALICE equivalent</li>
 * <li>Powerful means that we expose all the functions ALICE has available at equivalent or faster speeds</li>
 * <li>Flexible means that we can add functions above and beyond what ALICE can provide.  Additionally we can connect to a number of different datasource ranging from historical data to live trading platforms</li>
 * </ol>
 * The most basic guideline is that algorithm developers should only be focusing on
 * what to do not how to do things. As a concrete example, if an algorithm developer
 * wishes to retrieve a quote, they should only need to call a getQuote method.
 * They do not need to worry about implementation details such as whether the quote
 * comes from a database or a live streaming feed. Details to the extent practicable
 * should be completely abstracted away.
 * @author dnguyen
 *
 */
trait BackTestingAlgo extends Algo {

  // For profit tracker
  var brokerTrades: StringBuffer = null

  val mathLib = new MathLib

  // Trading data
  var tradingRecord = new StringBuffer

  // Universe of tradeable stocks
  val allStocks: ArrayList[String] = new ArrayList[String]

  // Implied variables
  var currentDateTime = new Date
  var currentSecurity = ""

  /**
   * Maximum individual trade size in dollars
   */
  val MAX_TRADE_VALUE = 100000 * getCurrencyMultiplier

  /**
   * Minimum individual trade size in dollars
   */
  val MIN_TRADE_VALUE = 5000 * getCurrencyMultiplier

  def addTradingRecord(record: String) {
    tradingRecord.append(record + "\r\n")
  }

  /**
   * Main algorithm body where most computations are performed
   */
  def algoBody() {

  }

  def algoEnd() {
    writeRecord()
    // below here is profit tracking related
    val files = new java.io.File("import").list()
    files.foreach(file => new VirtualBroker(file, this))
  }

  def init() {
    brokerTrades = new StringBuffer
  }

  /**
   * Is this a valid trading date? In other words not a weekend or holiday
   * @param date
   * @return
   */
  def isTradingDate(date: Date): Boolean = {
    connector.isTradingDate(getMarketName, date)
  }
  def isTradingDate(date: String): Boolean = {
    isTradingDate(lib.convertISODateTimeString(date))
  }

  def loadStocks() {
    val stocks = lib.openFile("data", "allstocks.csv")
    stocks.foreach(stock => allStocks.add(stock.trim.split("\\.")(0)))
  }

  /**
   * Open file on hard disk and as String array
   * @param dirName directory name
   * @param fileName file name
   * @String array one line per array element
   */
  def openFile(dirName: String, fileName: String): Array[String] = {
    lib.openFile(dirName, fileName)
  }

  /**
   * Track profit of a particular trade/exit pair
   * @param dateTime
   * @param stock
   * @param volume
   * @param price
   * @param exitTime
   * @param closeStrategy
   */
  def profitTrack(dateTime: Date, stock: String, volume: Int,
    price: Double, exitTime: Date, closeStrategy: String) {

    profitTrack(lib.dateToISODateTimeString(dateTime), stock, volume,
      price, lib.dateToISODateTimeString(exitTime), closeStrategy)
  }

  /**
   * Track profit of a particular trade/exit pair
   * @param entryTime
   * @param stock
   * @param volume
   * @param price
   * @param exitTime
   * @param closeStrategy Trade.CLOSE_AGGRESSIVE, Trade.CLOSE_MID,
   * Trade.CLOSE_PASSIVE, Trade.CLOSE_VWAP
   */
  def profitTrack(entryTime: String, stock: String, volume: Int,
                  price: Double, exitDateTime: String, closeStrategy: String) {


    brokerTrades.append(getMarketName + "," +
      entryTime + "," +
      stock + "," + volume
      + "," + price + "," +
      exitDateTime + "," +
      closeStrategy + "\r\n")
  }

  def runBackTest() {
    try {
           init()
    algoStart()
    algoBody()
    algoEnd()
    }
    catch {
      case e: Exception => e.printStackTrace
    }

  }

  /**
   * This method is called at the startOfDay of the algorithm.  It should be used to perform initialisation
   */
  def algoStart() {
    setCSVHeader(getCSVHeader)

  }

  /**
   * Takes a date object and a time string in the
   * format HH:mm:ss.SSS and converts to a date object.
   * date Date
   * time String
   * @return
   */
  def combineDateTime(date: Date, time: String): Date = {
    lib.combineDateTime(date, time)
  }

  /**
   * Takes a date string and a time string in the
   * format HH:mm:ss.SSS and converts to a date object.
   * date String
   * time String
   * @return
   */
  def combineDateTime(date: String, time: String): Date = {
    lib.combineDateTime(date, time)
  }

  /**
   * Takes a Date in DD/MM/YYYY and converts to Date object
   * @param date
   * @return
   */
  def convertSmartsDate(date: String): Date = {
    lib.convertSmartsDate(date)

  }

  /**
   * Takes a time in the format 01/03/2010 10:35:29 format and converts to
   * Date object
   * @param dateTime String in  01/03/2010 10:35:29.999 format
   * @return
   */
  def convertSmartsDateTime(dateTime: String): Date = {
    lib.convertSmartsDateTime(dateTime)
  }

  /**
   *
   * @param date
   * @"Mon", "Tue" ...
   */

  def dateToDay(date: Date): String = {
    lib.dateToDay(date)
  }

  /**
   * Takes a date object and converts to YYYY-MM-DD format
   * @param date
   * @return
   */
  def dateToISODateString(date: Date): String = {
    lib.dateToISODateString(date)
  }
  def dateToISODateTimeString(date: Date): String = {
    lib.dateToISODateTimeString(date)
  }

  /**
   *
   * @param date
   * @String in SMARTS format dd/MM/yyyy
   */
  def dateToSmartsDateString(date: Date): String = {
    lib.dateToSmartsDateString(date)
  }

  /**
   * Adjust volume according to max trade parameters
   * @param trade
   * @return
   */
  def adjustVolume(trade: Trade) {
    val value = trade.getPrice * trade.getVolume
    if (value > MAX_TRADE_VALUE) {
      val volume = (MAX_TRADE_VALUE / trade.getPrice).toInt
      trade.setVolume(volume)
    }
  }

  /**
   * Get all securities that traded on a given date
   * @param date Date in ISO format
   * @return
   */
  def getAllTradedSecurities(date: Date): Array[String] = {
    getAllTradedSecurities(lib.dateToISODateString(date))
  }
  def getAllTradedSecurities(date: String): Array[String] = {
    connector.getAllTradedSecurities(getMarketName, date)
  }


  def getClosePrice(date: Date = getCurrentDateTime): Double = {
    connector.getClosePrice(getMarketName, getCurrentSecurity, date)
  }
  def getClosePrice(date: String): Double = {
    getClosePrice(lib.convertISODateTimeString(date))
  }

  /**
   * Retrieve closing trade for specific security and date
   * @param security
   * @param date
   * @return
   */
  def getClosingTrade(date: String): Trade = {
    connector.getClosingTrade(getMarketName, getCurrentSecurity, date close, date endAuctionClose)
  }

  protected def getCSVHeader: String = ""

  /**
   * Date object capturing details of current
   * quote or trade
   * @return
   */
  def getCurrentDateTime: Date = {
    currentDateTime
  }

  /**
   * String object capturing details of current
   * quote or trade
   * @String representing security code
   */
  def getCurrentSecurity: String = {
    currentSecurity
  }

  /**
   * Takes a date object and converts to a date object at day endOfDay.
   * date Date
   * @return
   */
  def getDateEnd(date: Date): Date = {
    lib.getDateEnd(date)
  }
  /**
   * Takes a date string and converts to a date object at day endOfDay.
   * date String
   * @return
   */
  def getDateEnd(date: String): Date = {
    getDateEnd(lib.convertISODateTimeString(date))
  }

  /**
   * Takes a date object and converts to a date object at day startOfDay.
   * date Date
   * @return
   */
  def getDateStart(date: Date): Date = {
    lib.getDateStart(date)
  }
  /**
   * Takes a date string and converts to a date object at day startOfDay.
   * date String
   * @return
   */
  def getDateStart(date: String): Date = {
    getDateStart(lib.convertISODateTimeString(date))
  }

  /**
   * Get first trade price
   * @return
   */
  def getFirstPrice: Double = {
    val startDateTime = lib.getDateStart(getCurrentDateTime)
    getFirstPrice(startDateTime, getCurrentDateTime)
  }
  def getFirstPrice(dateStart: Date): Double = {
    getFirstPrice(dateStart, getCurrentDateTime)
  }
  def getFirstPrice(dateStart: Date, dateEnd: Date): Double = {
    connector.getFirstPrice(getMarketName, getCurrentSecurity, dateStart, dateEnd)
  }
  def getFirstPrice(date: String): Double = {
    getFirstPrice(lib.convertISODateTimeString(date))
  }

  /**
   * Get last trade price
   * @return
   */
  def getLastPrice: Double = {
    val startDateTime = lib.getDateStart(getCurrentDateTime)
    getLastPrice(startDateTime, getCurrentDateTime)
  }
  def getLastPrice(dateStart: Date): Double = {
    getLastPrice(dateStart, getCurrentDateTime)
  }
  def getLastPrice(dateStart: Date, dateEnd: Date): Double = {
    connector.getLastPrice(getMarketName, getCurrentSecurity, dateStart, dateEnd)
  }
  def getLastPrice(date: String): Double = {
    getLastPrice(lib.convertISODateTimeString(date))
  }

  /**
   * latest - Must be before this date
   */
  def getLastTransactionDate(latest: java.util.Date): Date = {
    connector.getLastTransactionDate(getMarketName, latest)
  }

  /**
   * Get maximum trade price
   * @return
   */

  def getMaxPrice(dateStart: Date = lib.getDateStart(getCurrentDateTime), 
      dateEnd: Date = getCurrentDateTime): Double = {
    connector.getMaxPrice(getMarketName, getCurrentSecurity, dateStart, dateEnd)
  }
  def getMaxPrice(maxEvents: Int) = {
    connector.getMaxPrice(getMarketName, getCurrentSecurity, getCurrentDateTime, maxEvents)
  }
  def getMaxPrice(startTime: String): Double = {
    val startDateTime = lib.combineDateTime(lib.dateToISODateString(getCurrentDateTime), startTime)
    getMaxPrice(startDateTime)
  }
  def getMaxPrice(startTime: String, endTime: String): Double = {
    val todayDate = lib.dateToISODateString(getCurrentDateTime)
    val startDateTime = lib.combineDateTime(todayDate, startTime)
    val endDateTime = lib.combineDateTime(todayDate, endTime)
    getMaxPrice(startDateTime, endDateTime)
  }

  /**
   * Adjust volume according to max trade parameters
   * @return
   */
  def getMaxVolume(price: Double, volume: Int): Int = {
    var maxVolume = volume
    var value = price * volume
    if (value > MAX_TRADE_VALUE) {
      maxVolume = (MAX_TRADE_VALUE / price).toInt
    }
    maxVolume
  }

  /**
   * Get minimum trade price
   * @return
   */
  def getMinPrice: Double = {
    val startDateTime = lib.getDateStart(getCurrentDateTime)
    getMinPrice(startDateTime, getCurrentDateTime)
  }
  def getMinPrice(dateStart: Date): Double = {
    getMinPrice(dateStart, getCurrentDateTime)
  }
  def getMinPrice(dateStart: Date, dateEnd: Date): Double = {
    connector.getMinPrice(getMarketName, getCurrentSecurity, dateStart, dateEnd)
  }
  def getMinPrice(maxEvents: Int): Double = {
    connector.getMinPrice(getMarketName, getCurrentSecurity, getCurrentDateTime, maxEvents)
  }
  def getMinPrice(startTime: String): Double = {
    val startDateTime = lib.combineDateTime(lib.dateToISODateString(getCurrentDateTime), startTime)
    getMinPrice(startDateTime)
  }
  def getMinPrice(startTime: String, endTime: String): Double = {
    val todayDate = lib.dateToISODateString(getCurrentDateTime)
    val startDateTime = lib.combineDateTime(todayDate, startTime)
    val endDateTime = lib.combineDateTime(todayDate, endTime)
    getMinPrice(startDateTime, endDateTime)
  }

  /**
   * Get minimum spread for given date
   * @param date
   * @return
   */
  def getMinSpread(date: String): Double = {
    connector.getMinSpread(getMarketName, getCurrentSecurity, date, date, true)
  }

  /**
   * Get opening price
   * @return
   */
  def getOpenPrice: Double = {
    getOpenPrice(getCurrentDateTime)
  }
  def getOpenPrice(date: Date): Double = {
    connector.getOpenPrice(getMarketName, getCurrentSecurity, date)
  }
  def getOpenPrice(date: String): Double = {
    getOpenPrice(lib.convertISODateTimeString(date))
  }

  def getOpeningTrade(security: String, date: String): Trade = {
    connector.getOpeningTrade(getMarketName, security, date)
  }

  /**
   * Retrieve quote for current date and time
   * @param date Date in YYYY-MM-DD format
   * @param time Time in HH:mm:ss.SSS format
   * @Quote at specified date and time
   */
  def getQuote(date: String, time: String): Quote = {
    connector.getQuote(getMarketName, getCurrentSecurity, date, time)
  }

  def getRegression(x: Array[Double], y: Array[Double], xValue: Double, confidence: Double): Regression = {
    mathLib.getRegression(x, y, xValue, confidence)
  }

  // ***************************************************************
  // TCOUNT FUNCTIONS
  // ***************************************************************

  /**
   * Get transaction count between two dates (inclusive).
   * @int
   */
  def getTcount: Int = {
    val startDateTime: Date = lib.getDateStart(getCurrentDateTime)
    getTcount(startDateTime, getCurrentDateTime)
  }

  /**
   * Get transaction count between two dates (inclusive).
   */
  def getTcount(dateStart: String, dateEnd: String): Int = {
    connector.getTcount(getMarketName, getCurrentSecurity, dateStart, dateEnd)
  }
  /**
   * Get transaction count between two dates (inclusive).
   * @param startDateTime
   * @param endDateTime
   * @param maxEvents
   * @int
   */
  def getTcount(startDateTime: Date, endDateTime: Date): Int = {
    connector.getTcount(getMarketName, getCurrentSecurity, startDateTime, endDateTime)
  }

  /**
   * a trading date with specified offset
   * @param date Date as date object
   * @param offset 2 means 2 trading days ahead, -1 means 1 trading day back
   * @date as Date
   */
  def getTradingDate(date: Date, offset: Int): String = {
    getTradingDate(lib.dateToISODateString(date), offset)
  }
  /**
   * a trading date with specified offset
   * @param date as String
   * @param offset 2 means 2 trading days ahead, -1 means 1 trading day back
   * @date as String
   */
  def getTradingDate(date: String, offset: Int): String = {
    connector.getTradingDate(getMarketName, date, offset)
  }

  // ***************************************************************
  // VALUE FUNCTIONS
  // ***************************************************************

  /**
   * Get transaction dollar value between two dates (inclusive).
   * @double rounded to 4 decimal places
   */
  def getValue: Double = {
    val startDateTime = lib.getDateStart(getCurrentDateTime)
    getValue(startDateTime, getCurrentDateTime, 0)
  }

  /**
   * Get transaction dollar value between two dates (inclusive).
   */
  def getValue(dateStart: String, dateEnd: String): Double = {
    connector.getValue(getMarketName, getCurrentSecurity, dateStart, dateEnd)
  }

  /**
   * Calculates value between two dates (inclusive). Limit results to maxEvents.
   * Negative (positive) maxEvents rolls backwards (forwards) in time.
   * @param startDateTime
   * @param endDateTime
   * @param maxEvents
   * @double rounded to 4 decimal places
   */
  def getValue(startDateTime: Date, endDateTime: Date, maxEvents: Int): Double = {
    connector.getValue(getMarketName, getCurrentSecurity, startDateTime, endDateTime, maxEvents)
  }

  // ***************************************************************
  // VOLUME FUNCTIONS
  // ***************************************************************

  /**
   * Get transaction volume between two dates (inclusive).
   * @int
   */
  def getVolume: Int = {
    val startDateTime = lib.getDateStart(getCurrentDateTime)
    getVolume(startDateTime, getCurrentDateTime, 0)
  }

  /**
   * Calculates volume between two dates (inclusive). Limit results to maxEvents.
   * Negative (positive) maxEvents rolls backwards (forwards) in time.
   * @param startDateTime
   * @param endDateTime
   * @param maxEvents
   * @int
   */
  def getVolume(startDateTime: Date, endDateTime: Date, maxEvents: Int): Int = {
    connector.getVolume(getMarketName, getCurrentSecurity, startDateTime, endDateTime, maxEvents)
  }

  // ***************************************************************
  // VWAP FUNCTIONS
  // ***************************************************************

  /**
   * Calculates VWAP between startOfDay of today and now.
   * @double rounded to 4 decimal places
   */
  def getVWAP: Double = {
    val startDateTime = lib.getDateStart(getCurrentDateTime)
    getVWAP(startDateTime, getCurrentDateTime)
  }

  /**
   * Calculates VWAP between startOfDay date and now (inclusive).
   * @param startDateTime, a date object
   * @double rounded to 4 decimal places
   */
  def getVWAP(startDateTime: Date): Double = {
    getVWAP(startDateTime, getCurrentDateTime)
  }

  /**
   * Calculates VWAP between two dates (inclusive).
   * @param startDate
   * @param endDate
   * @double rounded to 4 decimal places
   */
  def getVWAP(startDateTime: Date, endDateTime: Date): Double = {
    getVWAP(startDateTime, endDateTime, 0)
  }

  /**
   * Calculates VWAP between two dates (inclusive). Limit results to maxEvents.
   * Negative (positive) maxEvents rolls backwards (forwards) in time.
   * @param startDateTime
   * @param endDateTime
   * @param maxEvents
   * @double rounded to 4 decimal places
   */
  def getVWAP(startDateTime: Date, endDateTime: Date, maxEvents: Int): Double = {
    connector.getVWAP(getMarketName, getCurrentSecurity, startDateTime, endDateTime, maxEvents)
  }

  /**
   * Calculates VWAP starting at a specified date. Limit results to maxEvents.
   * Negative (positive) maxEvents rolls backwards (forwards) in time.
   * @param date, a Date
   * @param maxEvents
   * @double rounded to 4 decimal places
   */
  def getVWAP(date: Date, maxEvents: Int): Double = {

    var startDateTime: Date = date
    var endDateTime: Date = date

    if (maxEvents > 0) {
      startDateTime = date
      endDateTime = lib.getDateEnd(date)
    } else if (maxEvents < 0) {
      startDateTime = lib.getDateStart(date)
      endDateTime = getCurrentDateTime
    } else {
      startDateTime = date
      endDateTime = getCurrentDateTime
    }

    getVWAP(startDateTime, endDateTime, maxEvents)
  }

  /**
   * Calculates VWAP between two times on a given date.
   * @param date, a Date object
   * @param startTime, a String
   * @param endTime, a String
   * @double rounded to 4 decimal places
   */
  def getVWAP(date: Date, startTime: String, endTime: String): Double = {
    val startDateTime = lib.combineDateTime(date, startTime)
    val endDateTime = lib.combineDateTime(date, endTime)
    getVWAP(startDateTime, endDateTime)
  }

  /**
   * Calculates VWAP between time and now.
   * @param startTime, a String
   * @double rounded to 4 decimal places
   */
  def getVWAP(startTime: String): Double = {
    val startDateTime = lib.combineDateTime(lib.dateToISODateString(getCurrentDateTime), startTime)
    getVWAP(startDateTime, getCurrentDateTime)
  }

  /**
   * Calculates VWAP between two times.
   * @param startTime, a String
   * @param endTime, a String
   * @double rounded to 4 decimal places
   */
  def getVWAP(startTime: String, endTime: String): Double = {
    val todayDate = lib.dateToISODateString(getCurrentDateTime)
    val startDateTime = lib.combineDateTime(todayDate, startTime)
    val endDateTime = lib.combineDateTime(todayDate, endTime)
    getVWAP(startDateTime, endDateTime)
  }

  /**
   * Calculates VWAP starting at a specified time. Limit results to maxEvents.
   * Negative (positive) maxEvents rolls backwards (forwards) in time.
   * @param time, a String
   * @param maxEvents
   * @double rounded to 4 decimal places
   */
  def getVWAP(time: String, maxEvents: Int): Double = {

    var startDateTime: Date = new Date
    var endDateTime: Date = new Date
    var todayDate = lib.dateToISODateString(getCurrentDateTime)

    if (maxEvents > 0) {
      startDateTime = lib.combineDateTime(todayDate, time)
      endDateTime = getDateEnd(todayDate)
    } else if (maxEvents < 0) {
      startDateTime = getDateStart(todayDate)
      endDateTime = lib.combineDateTime(todayDate, time)
    } else {
      startDateTime = getDateStart(todayDate)
      endDateTime = getCurrentDateTime
    }

    getVWAP(startDateTime, endDateTime, maxEvents)
  }

  // ***************************************************************

  /**
   * Round numbers
   * @param double
   */
  def round0(unrounded: Double): Double = {
    connector.round0(unrounded)
  }
  def round1(unrounded: Double): Double = {
    connector.round1(unrounded)
  }
  def round2(unrounded: Double): Double = {
    connector.round2(unrounded)
  }
  def round3(unrounded: Double): Double = {
    connector.round3(unrounded)
  }
  def round4(unrounded: Double): Double = {
    connector.round4(unrounded)
  }

  def setCSVHeader(header: String) {
    tradingRecord = new StringBuffer(header + "\r\n")
  }

  /**
   * Set the current time of the quote, order or trade
   * the algo is processing
   * @param date should contain time and date
   */
  def setCurrentDateTime(date: Date) {
    currentDateTime = date
  }

  /**
   * Set the current security of the quote, order or trade
   * the algo is processing
   * @param security String representation of security
   */
  def setCurrentSecurity(security: String) {
    currentSecurity = security
  }


  /**
   * Convert Double ArrayList to double array
   * @param list
   * @return
   */
  def toDoubleArray(list: ArrayList[java.lang.Double]): Array[Double] = {
    lib.toDoubleArray(list)
  }

  def writeFile(fileName: String, data: String) {
    lib.writeFile(".", fileName, data)
  }

  def writeRecord() {
    lib.writeFile(".", "trades.csv", tradingRecord.toString)
    lib.writeFile("import", "brokertrades.csv", brokerTrades.toString)
  }

}
