package com.alluvia.algo

import datasource.Historical
import scala.collection.mutable.HashMap
import com.alluvia.fix.Heartbeat
import com.alluvia.types.benchmark.BTrade
import com.alluvia.patterns.Observer
import org.apache.commons.math.stat.descriptive.SummaryStatistics
import java.util.Date
import quickfix.field.ExecType
import com.alluvialtrading.fix.{OrderType, Order, OrderSide}
import com.alluvialtrading.fix.OrderSide._
import com.alluvia.types.market._
import com.alluvia.types.ObservedEventPump
import fix._

trait EventAlgo extends Algo with Observer[Any] with BackTestingAlgo {

  type Security = String

  private val lastBidQuote = new HashMap[String, com.alluvial.mds.contract.Quote]
  private val lastAskQuote = new HashMap[String, com.alluvial.mds.contract.Quote]
  private val lastTrade = new HashMap[String, com.alluvial.mds.contract.Trade]
  private val lastQuoteMatch = new HashMap[String, com.alluvial.mds.contract.QuoteMatch]
  private val dailyTrades = new HashMap[String, Int]
  private val dailyValues = new HashMap[String, Double]
  protected val distributions = new HashMap[String, SummaryStatistics]

  // market close time for current date
  protected var closeTime = new java.util.Date
  protected var openTime = new java.util.Date
  protected var auctionNearFinishTime = new java.util.Date()
  protected var uncrossTime = new java.util.Date()

  // Time zone testing
  protected var testTime = new java.util.Date()

  // Maximum number of trades per algo
  def maxOrders: Int
  var currentTradesAttemptedToday = 0

  private var date = new Date
  private var security = ""

  // Trade variables
  private var price = 0.0
  private var volume = 0.0
  private var value = 0.0
  private var bidOrAsk = 'A'

  private var openPrice = 0.0
  private var maxPrice = 0.0
  private var minPrice = 0.0
  private var closePrice = 0.0
  private var securityStatus = 'P'

  // prevent excessive garbage collection
  var masterQuote = Quote(date,
    security,
    price,
    Double.NaN,
    Double.NaN,
    volume,
    value,
    Double.NaN,
    Double.NaN,
    bidBefore(security),
    askBefore(security),
    bidVolBefore(security),
    askVolBefore(security),
    bidOrAsk)

  var masterOffMarketTrade = OffMarketTrade(date, security, price, volume, value, askBefore(security), bidBefore(security))
  var masterSingleOrder = SingleOrder(0L, date, security, price, volume, value, askBefore(security), bidBefore(security), 'A', 'A', 0)
  var masterQuoteFull = QuoteFull(date, security, openPrice, maxPrice, minPrice, closePrice, securityStatus)

  // Summary variables
  var highPrice = 0.0
  var lowPrice = 0.0

  var transType = ""

  // Params
  def delayBetweenTrades = 4.second

  // Parse key=value
  def extractFIX(raw: String) = raw.split("=")(1)

  // Should we "force" market prices in back testing or accept the prices
  // provided by the algorithm?
  def strictPricing = true

  // Trading parameters
  def numBuys = getOpenBuySells.filter(_ == BUY).size
  def numSells = getOpenBuySells.filter(_ == SELL).size
  def getAllOrders = openOrdersByTicker.map(x => x._2).toList
  // Retrieve any submitted order regardless of cancellation status
  def getAnyOrderByID(id: String) = allOrdersByID(id)
  def getBuys = openOrdersByTicker.filter(_._2.getSide == BUY).map(x => x._2).toList
  def getSells = openOrdersByTicker.filter(_._2.getSide == SELL).map(x => x._2).toList
  def getWorstBuy = openOrdersByTicker.map(x => x._2).filter(_.getSide == BUY).toList.sortBy(x => x.getScore).head.getSymbol
  def getWorstSell = openOrdersByTicker.map(x => x._2).filter(_.getSide == SELL).toList.sortBy(x => x.getScore).head.getSymbol

  def generateOrder(side: OrderSide, symbol: String, limit: Double, quantity: Int): Order = {
    val order = new Order()
    order.setSide(side)
    order.setSymbol(symbol)
    order.setLimit(limit)
    order.setQuantity(quantity)
    order.setType(OrderType.LIMIT)
    order
  }

  private def getOpenOrders = openOrdersByTicker.values.toList
  def getOpenBuySells = getOpenOrders.map(x => x.getSide)
  def getOpenSecurities: List[String] = getOpenOrders.map(x => x.getTicker)
  def getOrderByID(ID: String) = openOrdersByID(ID)
  def getOrderBySymbol(security: String) = openOrdersByTicker(security)
  def hasOpenOrder(security: String) = openOrdersByTicker.contains(security)
  def isHistorical = this.isInstanceOf[Historical]

  // Latest prices for profit tracking
  val latestPrices = new HashMap[String, Double]

  // Should the algo exit its own positions
  def performExit = false

  // For Profit tracking
  private val openOrdersByTicker = new HashMap[String, Order]
  val openOrdersByID = new HashMap[String, Order]

  // Store everything irrespective of cancellation status
  val allOrdersByID = new HashMap[String, Order]
  val brokerage = 0.0002
  var totalProfit = 0.0

  private def askBefore(security: String): Double = {
    val result = lastAskQuote.get(security)
    result match {
      case None => Double.NaN
      case Some(x) => (x.Price)
    }
  }

  private def bidBefore(security: String): Double = {
    val result = lastBidQuote.get(security)
    result match {
      case None => Double.NaN
      case Some(x) => (x.Price)
    }
  }

  private def askVolBefore(security: String): Double = {
    val result = lastAskQuote.get(security)
    result match {
      case None => Double.NaN
      case Some(x) => (x.Volume)
    }
  }

  private def bidVolBefore(security: String): Double = {
    val result = lastBidQuote.get(security)
    result match {
      case None => Double.NaN
      case Some(x) => (x.Volume)
    }
  }

  def storeLimitOrder(order: Order) {

    val security = order.getTicker
    if (openOrdersByTicker.contains(security)) {
      val existing = openOrdersByTicker(security)

      // Build position
      if (order.getSide == existing.getSide) {

      }
      else {
        // Use avgpx to store actual prices which may differ to
        // submitted (possibly aggressive) prices
        val existingPrice = existing.getAvgPx
        val orderPrice = order.getAvgPx
        val existingBrokerage = existingPrice * existing.getQuantity * brokerage
        val newBrokerage = orderPrice * order.getQuantity * brokerage
        val totalBrokerage = existingBrokerage + newBrokerage
        val profit = existing.getSide match {
          case BUY => (orderPrice - existingPrice) * order.getQuantity - totalBrokerage
          case _ => (existingPrice - orderPrice) * order.getQuantity - totalBrokerage
        }
        //submitLimitOrder(direction, security, price, quantity)
        totalProfit += profit

        // Profit track
        println(date, "Open", security, existing.getSide, existingPrice, "Close", orderPrice, "Brokerage", totalBrokerage, "Profit", profit, "Cumulative", totalProfit)
        printcsv("backtesting.csv", date.toIso, "Open", security, existing.getSide, existingPrice, "Close", orderPrice, "Brokerage", totalBrokerage, "Profit", profit, "Cumulative", totalProfit)

        // Remove close trade
        openOrdersByTicker.remove(security)
      }
    }
    else {
      openOrdersByTicker.put(security, order)
      openOrdersByID.put(order.getID, order)
      allOrdersByID.put(order.getID, order)
    }


  }

  def tradeVol: Double = {
    val result = lastTrade.get(security)
    result match {
      case None => Double.NaN
      case Some(x) => x.TradeVolume
    }
  }

    /**
   * default stub
   */
  def cancelOrder(order: Order) {
  }

  def createOrderWithoutSend(side: OrderSide, symbol: String, limit: Double, quantity: Int): Order = {
    val order = new Order()
    order.setSide(side)
    order.setSymbol(symbol)
    order.setLimit(limit)
    order.setQuantity(quantity)
    order.setType(OrderType.LIMIT)
    order
  }

  def dailyValue: Double = {
    val result = dailyValues.get(security)
    result match {
      case None => Double.NaN
      case Some(x) => x
    }
  }

  def spread(security: String) = {
    if (transType == "Trade") {
      askBefore(security) - bidBefore(security)
    }
    else if (transType == "Quote") {
      if (bidOrAsk == 'A') {
        if (price > bidBefore(security))
          price - bidBefore(security)
        else spreadBefore
      }
      else if (bidOrAsk == 'B') {
        if (askBefore(security) > price)
          askBefore(security) - price
        else spreadBefore
      }
      else {
        println("bidOrAsk undefined")
      }
    }
    else {
      undefined
    }
  }

  private def spreadBefore = askBefore(security) - bidBefore(security)
  def numTrades: Int = dailyTrades.size
  val undefined = Double.NaN
  

//  // Quote variables
//  var bidOrAsk = 'A'

  protected def benchmarkDays = 0

  def addDistribution(security: String, value: Double) {
    if (!distributions.contains(security)) {
      val distribution = new SummaryStatistics()
      distribution.addValue(value)
      distributions.put(security, distribution)
    }
    else {
      val distribution = distributions(security)
      distribution.addValue(value)
      distributions.put(security, distribution)
    }
  }

  def printToBrowser = true // prod = true
  def processEvent(event: Any) {
    synchronized {
      event match {

        case event: Control => onControl(event)
        case event: DayEnd => masterOnDayEnd(event)
        case event: DayStart => masterOnDayStart(event)
        case event: End => masterOnEnd(event)
        case event: BTrade => masterOnTrade(event)
        case event: com.alluvial.mds.contract.Trade => masterOnTrade(event)
        case event: com.alluvial.mds.contract.Quote => masterOnQuote(event)
        case event: com.alluvial.mds.contract.QuoteFull => masterOnQuoteFull(event)
        case event: com.alluvial.mds.contract.QuoteMatch => masterOnQuoteMatch(event)
        case event: Start => masterOnStart(event)
        case event: com.alluvial.mds.contract.SingleOrder => masterOnSingleOrder(event)
        case event: Heartbeat => masterOnFIXHeartbeat(event)
        case event: quickfix.fix42.ExecutionReport => masterOnFIXExecutionReport(event)
        case event: quickfix.fix42.OrderCancelReject => masterOnFIXOrderCancelReject(event)
        case event: com.alluvial.mds.contract.OffMktTrade => masterOnOffMarketTrade(event)
        case event: com.alluvial.mds.contract.MDSInfo => println("mdsinfo")
        case event: com.alluvial.mds.contract.DictionaryResponse => println("dictionary response")
        case event: com.alluvial.mds.contract.SubscriptionConfirmation => println("subscription confirmation")
        case event: com.alluvial.mds.contract.ReplayDateStart => println("replay date start")
        case event: com.alluvial.mds.contract.ReplayDateEnd => println("replay day end")
        case event: com.alluvial.mds.contract.ReplayConfirmation =>
          println((new Date).toString + "replay confirmation")
          Thread.sleep(5000)
        case _ => println("NO MATCH for: " + event)
      }
    }
  }

  def allSecurities = lastQuoteMatch.keySet.view.filter(
    s => lastAskQuote.contains(s) && lastBidQuote.contains(s)).map {
    currentSecurity =>
      security = currentSecurity
      // back testing
      setCurrentSecurity(security)
      security
  }

  /**
   * default stub
   */
  def amendOrder(oldOrder: Order, newOrder: Order) {
  }

//  def isCrossed(security: String): Boolean = {
//
//    if (transType == Quote || transType == "QuoteMatch") {
//      if (bid(security) >= ask(security))
//        return true
//      else return false
//    }
//    false
//
//
//  }


  def login() {

  }

  def defined(obj: Any) = obj match {
    case obj: Double => !obj.isNaN
    case _ => true
  }
  def defined[T,U](hash: HashMap[T, U], key: T) = hash.contains(key)

  def undefined(obj: Any) = !defined(obj)
  def undefined[T,U](hash: HashMap[T, U], key: T) = !defined(hash, key)

  private def masterOnDayEnd(dayEnd: DayEnd) {
    date = dayEnd.dateTime
    onDayEnd(dayEnd)
  }

  private def masterOnDayStart(dayStart: DayStart) {
    auctionNearFinishTime = lib.combineDateTime(dayStart.dateTime, getEndAuctionClose)
    closeTime = lib.combineDateTime(dayStart.dateTime, getCloseTime)
    openTime = lib.combineDateTime(dayStart.dateTime, getOpenTime)
    date = dayStart.dateTime
    testTime = lib.combineDateTime(dayStart.dateTime, "15:25:00")
    uncrossTime = lib.combineDateTime(dayStart.dateTime, getUncrossTime)
    currentTradesAttemptedToday = 0
    //openOrdersBySymbol.clear()
    onDayStart(dayStart)
  }

  private def masterOnEnd(end: End) {
    onEnd(end)
  }

  private def masterOnFIXExecutionReport(message: quickfix.fix42.ExecutionReport) {

    val execType = message.getExecType.getValue

    //security = message.getSymbol.toString
    //price = message.getPrice.getValue
   // volume = message.getOrderQty.getValue.toInt
    //ms
    execType match {
      case ExecType.NEW =>
        //println("NEW")
        onFIXNew(FIXNew(message.getOrderID.toString, message.getClOrdID.toString, message.getSymbol.toString, message.getLastPx.getValue, message.getLastShares.getValue.toInt))
      case ExecType.PARTIAL_FILL => onFIXPartialFill(FIXPartialFill(message.getOrderID.toString, message.getClOrdID.toString, message.getSymbol.toString, message.getLastPx.getValue, message.getLastShares.getValue.toInt))
      case ExecType.FILL => onFIXFill(FIXFill(message.getOrderID.toString, message.getClOrdID.toString, message.getSymbol.toString, message.getLastPx.getValue, message.getLastShares.getValue.toInt))
      case ExecType.CANCELED => onFIXCancel(FIXCancel(message.getOrderID.toString))
      case ExecType.REPLACE => print("replace")
      case ExecType.REJECTED =>
        val order = getAnyOrderByID(extractFIX(message.getClOrdID.toString))
        // Remove order
        if (openOrdersByTicker.contains(order.getTicker) &&
          openOrdersByID.contains(order.getID)) {
          openOrdersByTicker.remove(order.getTicker)
          openOrdersByID.remove(order.getID)
          println("EventAlgo removing due to rejection: " + order.getTicker + date)
        }
        onFIXReject(FIXReject(message.getClOrdID.toString, message.getOrderID.toString, message.getOrdRejReason.toString))
      //case ExecType.
      case _ => print(execType)
    }

  }

  private def masterOnFIXHeartbeat(heartbeat: Heartbeat) {
    onFIXHeartbeat()
  }

  private def masterOnFIXOrderCancelReject(message: quickfix.fix42.OrderCancelReject) {
    onFIXOrderCancelReject(FIXOrderCancelReject(message.getOrigClOrdID.toString))
  }
  private def masterOnOffMarketTrade(trade: com.alluvial.mds.contract.OffMktTrade) {

    trade.TradePrice /= getCurrencyMultiplier

    security = trade.Security
    date = new Date(trade.TradeTime)
    price = trade.TradePrice
    volume = trade.TradeVolume
    value = (price * volume)

    masterOffMarketTrade.date = date
    masterOffMarketTrade.security = security
    masterOffMarketTrade.price = price
    masterOffMarketTrade.volume = volume
    masterOffMarketTrade.value = value
    masterOffMarketTrade.askBefore = askBefore(security)
    masterOffMarketTrade.bidBefore = bidBefore(security)
    onOffMarketTrade(masterOffMarketTrade)
  }

  private def masterOnQuote(quote: com.alluvial.mds.contract.Quote) {

    transType = "Quote"
    quote.Price /= getCurrencyMultiplier

    date = new Date(quote.UpdateTime)
    price = (quote.Price)
    volume = quote.Volume
    value = (price * volume)
    security = quote.Security
    bidOrAsk = quote.BidOrAsk


  def ask(security: String): Double = if (bidOrAsk == 'A' && price > bidBefore(security)) price else if (!defined(bidBefore(security))) price else askBefore(security)
  def bid(security: String): Double = if (bidOrAsk == 'B' && price < askBefore(security)) price else bidBefore(security)

  def askVol(security: String): Double = if (bidOrAsk == 'A') volume else askVolBefore(security)
  def bidVol(security: String): Double = if (bidOrAsk == 'B') volume else bidVolBefore(security)

    // Avoid auctions
    if (date.before(closeTime)) {

      if (quote.Security == "VBA") printcsv("vbaraw.csv", new java.util.Date(quote.UpdateTime), quote.Security, quote.BidOrAsk, quote.Price, quote.Volume)
      // CURRENT
      // *************************************************************

      masterQuote.date = date
      masterQuote.security = security
      masterQuote.price = price
      masterQuote.volume = volume
      masterQuote.value = value
      masterQuote.ask = ask(security)
      masterQuote.bid = bid(security)
      masterQuote.askBefore = askBefore(security)
      masterQuote.bidBefore = bidBefore(security)
      masterQuote.askVol = askVol(security)
      masterQuote.bidVol = bidVol(security)
      masterQuote.askVolBefore = askVolBefore(security)
      masterQuote.bidVolBefore = bidVolBefore(security)
      masterQuote.bidOrAsk = bidOrAsk

      onQuote(masterQuote)
      //onQuote(Quote(date,security,price,volume,value,askBefore,bidBefore))

      // Update
      if (quote.BidOrAsk == 'B') {
        lastBidQuote.put(quote.Security, quote)
      }
      else if (quote.BidOrAsk == 'A') {
        lastAskQuote.put(quote.Security, quote)
      }
      else {
        println("Error in masterOnQuote")
      }

    }

  }

  private def masterOnQuoteFull(quoteFull: com.alluvial.mds.contract.QuoteFull) {

    //    quoteFull.AskPrice /= getCurrencyMultiplier
    //    quoteFull.BidPrice /= getCurrencyMultiplier
    //    security = quoteFull.Security
    //    price = quoteFull.LastPrice
    //    highPrice = quoteFull.HighPrice
    //    lowPrice = quoteFull.LowPrice
    //    onQuoteFull()

    transType = "QuoteFull"
    date = new Date(quoteFull.UpdateTime)
    security = quoteFull.Security
    openPrice = quoteFull.OpenPrice
    maxPrice = quoteFull.HighPrice
    minPrice = quoteFull.LowPrice
    closePrice = quoteFull.ClosePrice
    securityStatus = quoteFull.SecurityStatus

    masterQuoteFull.date = date
    masterQuoteFull.security = security
    masterQuoteFull.openPrice = openPrice
    masterQuoteFull.maxPrice = maxPrice
    masterQuoteFull.minPrice = minPrice
    masterQuoteFull.closePrice = closePrice
    masterQuoteFull.securityStatus = securityStatus

    onQuoteFull(masterQuoteFull)

  }

  private def masterOnQuoteMatch(quoteMatch: com.alluvial.mds.contract.QuoteMatch) {
    transType = "QuoteMatch"
    lastQuoteMatch.put(quoteMatch.Security, quoteMatch)
    quoteMatch.IndicativePrice /= getCurrencyMultiplier
    date = new Date(quoteMatch.UpdateTime)
    security = quoteMatch.Security
    price = quoteMatch.IndicativePrice
    volume = quoteMatch.MatchVolume
    value = price * volume

    latestPrices.put(security, price)

    if (price > 0)
      onQuoteMatch(QuoteMatch(date, security, price, volume, value, askBefore(security), bidBefore(security), quoteMatch.SurplusVolume))

  }


  private def masterOnSingleOrder(singleOrder: com.alluvial.mds.contract.SingleOrder) {

    transType = "SingleOrder"
    singleOrder.Price /= getCurrencyMultiplier
    date = new Date(singleOrder.UpdateTime)
    security = singleOrder.Security
    price = singleOrder.Price
    volume = singleOrder.Volume
    value = price * volume

    masterSingleOrder.orderNo = singleOrder.OrderNo
    masterSingleOrder.date = date
    masterSingleOrder.price = price
    masterSingleOrder.security = security
    masterSingleOrder.volume =volume
    masterSingleOrder.value = value
    masterSingleOrder.bidOrAsk = singleOrder.BidOrAsk
    masterSingleOrder.action = singleOrder.Action
    masterSingleOrder.orderType = singleOrder.OrderType

    onSingleOrder(masterSingleOrder)
  }

  private def masterOnStart(start: Start) {

    if (benchmarkDays > 0) {
      for (i <- 1 to benchmarkDays) {

      }
    }
    // Benchmarking
    onStart(start)
  }

  private def masterOnTrade(trade: BTrade) {
    trade.TradePrice /= getCurrencyMultiplier

    security = trade.Security
    date = new Date(trade.TradeTime)
    price = trade.TradePrice
    volume = trade.TradeVolume
    value = (price * volume)

    onBenchmarkTrade()
  }

  private def masterOnTrade(trade: com.alluvial.mds.contract.Trade) {

    transType = "Trade"
    trade.TradePrice /= getCurrencyMultiplier

    security = trade.Security
    date = new Date(trade.TradeTime)
    price = trade.TradePrice
    volume = trade.TradeVolume
    value = (price * volume)

    lastTrade.put(security, trade)
    if (!dailyTrades.contains(security)) dailyTrades.put(security, 0)
    if (!dailyValues.contains(security)) dailyValues.put(security, 0)
    val tradeCount = dailyTrades(security) + 1

    dailyTrades.put(security, tradeCount)
    dailyValues.put(security, value)
    latestPrices.put(security, price)

    onTrade(Trade(date, security, price, volume, value, askBefore(security), bidBefore(security), askVolBefore(security), bidVolBefore(security)))
  }

  def receiveUpdate(event: Any) {
    event match {
      case pump: ObservedEventPump => processEvent(pump.currentEvent)
    }
  }


  def removeOrder(security: String) {

    if (openOrdersByTicker.contains(security)) {

      val order = getOrderBySymbol(security)
      cancelOrder(order)
      openOrdersByTicker.remove(security)
      openOrdersByID.remove(order.getID)
      println("Cancelling: " + security + date)
    }
  }

  /**
   * default stub
   */
  def submitLimitOrder(side: OrderSide, symbol: String, limit: Double, quantity: Int): Order = {
    submitLimitOrder(side, symbol, limit, quantity , 0.0)
  }

  def submitLimitOrder(side: OrderSide, symbol: String, limit: Double, quantity: Int, score: Double): Order = {
    currentTradesAttemptedToday += 1
    val order = generateOrder(side, symbol, limit, quantity)
    order.setScore(score)
    storeLimitOrder(order)
    order
  }

  def onBenchmarkTrade() {
    println("onBTrade ")
  }

  def onControl(control: Control) {
    println("onControl: " + control.message)

  }

  def onDayEnd(d: DayEnd) {
    println("onDayEnd" + date)

    // fudge quotematch objects
  }

  def onDayStart(d: DayStart) {
    println("onDayStart" + date)

    // fudge quotematch objects
  }

  def onEnd(end: End) {

  }

  def onFIXCancel(f: FIXCancel) {

  }

  def onFIXFill(f: FIXFill) {

  }

  def onFIXHeartbeat() {

  }

  def onFIXNew(f: FIXNew) {

  }
  def onFIXOrderCancelReject(f: FIXOrderCancelReject) {

  }

  def onFIXPartialFill(f: FIXPartialFill) {

  }
  def onFIXReject(f: FIXReject) {

  }
  def onOffMarketTrade(o: OffMarketTrade) {

  }
  def onQuote(q: Quote) {
  }

  def onQuoteFull(q: QuoteFull) {
//  def onQuoteFull() {
  }

  def onQuoteMatch(q: QuoteMatch) {

    //quoteMatch.
  }

  def onSingleOrder(o: SingleOrder) {

  }

  def onStart(start: Start) {

  }


  def onTrade(t: Trade) {

  }

  case class Benchmark(date: Date, security: String, open: Double, openVolume: Int,
                       close: Double, closeVolume: Int, high: Double, low: Double,
                       tCount: Int, volume: Double, value: Double)
}