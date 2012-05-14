package com.alluvia.algo
/**
import scala.collection.mutable.HashMap
<<<<<<< local
=======
import com.alluvia.fix.Heartbeat
import com.alluvia.types.{Control, DayEnd, DayStart, End, ObservedEventPump, Start}
import com.alluvia.types.benchmark.BTrade
import com.alluvia.patterns.Observer
import org.apache.commons.math.stat.descriptive.SummaryStatistics
import com.alluvial.mds.contract._
import java.util.Date
import quickfix.field.ExecType
import com.alluvialtrading.fix.{OrderType, Order, OrderSide}
>>>>>>> other

trait TradingLimits extends Algo {

  // Liquidity limits
  var limitDailyTradesMin = new HashMap[String, String, Int]
  var limitDailyValueMin = new HashMap[String, String, Double]

  // Message limits
  var limitMessagesSentMax = Int
  var limitMessagesSentPerAlgoMax = new HashMap[String, String, Int]
  var limitMessagesSentPerAlgoSecurityMax = new HashMap[String, String, Int]

  // Open order count limits
  var limitOpenOrderNumTotalMax = Int
  var limitOpenOrderNumPerAlgoMax = new HashMap[String, String, Int]
  var limitOpenOrderNumPerAlgoSecurityMax = new HashMap[String, String, Int]

  // Open order size limits
  var limitOpenOrderSizeTotalMax = Double
  var limitOpenOrderSizePerAlgoMax = new HashMap[String, String, Double]
  var limitOpenOrderSizePerAlgoSecurityMax = new HashMap[String, String, Double]
  var limitOpenOrderSizePerAlgoSecurityMin = new HashMap[String, String, Double]

  // Position size limits
  var limitPositionSizeTotalMax = Double
  var limitPositionSizePerAlgoMax = new HashMap[String, String, Double]
  var limitPositionSizePerAlgoSecurityMax = new HashMap[String, String, Double]


  // ================================================================================
  // GLOBAL LIMITS
  // ================================================================================

  limitMessagesSentMax = 100
  limitOpenOrderNumTotalMax = 20
  limitOpenOrderSizeTotalMax = 100000.00
  limitPositionSizeTotalMax = 100000.00


  // ================================================================================
  // LSE LIMITS
  // ================================================================================

  // Sunset limits
  // ------------------------------------------------------------

  // Liquidity limits
  limitDailyTradesMin("LSE", "SUNSET") = 20
  limitDailyValueMin("LSE", "SUNSET") = 20000.00

  // Message limits
  limitMessagesSentPerAlgoMax("LSE", "SUNSET") = 100
  limitMessagesSentPerAlgoSecurityMax("LSE", "SUNSET") = 10

  // Open order count limits
  limitOpenOrderNumPerAlgoMax("LSE", "SUNSET") = 20
  limitOpenOrderNumPerAlgoSecurityMax("LSE", "SUNSET") = 1

  // Open order size limits
  limitOpenOrderSizePerAlgoMax("LSE", "SUNSET") = 100000.00
  limitOpenOrderSizePerAlgoSecurityMax("LSE", "SUNSET") = 20000.00
  limitOpenOrderSizePerAlgoSecurityMin("LSE", "SUNSET") = 8000.00

  // Position size limits
  limitPositionSizePerAlgoMax("LSE", "SUNSET") = 100000.00
  limitPositionSizePerAlgoSecurityMax("LSE", "SUNSET") = 20000.00

  // iRev limits
  // ------------------------------------------------------------

<<<<<<< local
=======
  def bidBefore: Double = {

    val result = lastBidQuote.get(security)
    result match {
      case None => Double.NaN
      case Some(x) => (x.Price)
    }

  }

  def tradePrice: Double = {

    val result = lastTrade.get(security)
    result match {
      case None => Double.NaN
      case Some(x) => (x.TradePrice)
    }

  }

  def askVolBefore: Double = {
    val result = lastAskQuote.get(security)
    result match {
      case None => Double.NaN
      case Some(x) => x.Volume
    }
  }

  def bidVolBefore: Double = {
    val result = lastBidQuote.get(security)
    result match {
      case None => Double.NaN
      case Some(x) => x.Volume
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
  def cancelOrder(order: Order): Unit = {
  }

  def dailyValue: Double = {
    val result = dailyValues.get(security)
    result match {
      case None => Double.NaN
      case Some(x) => x
    }
  }

  def spread = {
    if (transType == "Trade") {
      askBefore - bidBefore
    }
    else if (transType == "Quote") {
      if (bidOrAsk == "A") {
        if (price > bidBefore)
          price - bidBefore
        else spreadBefore
      }
      else if (bidOrAsk == "B") {
        if (askBefore > price)
          askBefore - price
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

  def midBefore = (askBefore + bidBefore) / 2
  def spreadBefore = askBefore - bidBefore
  def spreadBeforePercent = 100 * (askBefore - bidBefore) / midBefore
  def numTrades: Int = dailyTrades.size
  val undefined = Double.NaN
  

  // Quote variables
  var bidOrAsk = ""

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

  def processEvent(event: Any): Unit = synchronized {
    event match {

      case event: Control => onControl(event)
      case event: DayEnd => masterOnDayEnd(event)
      case event: DayStart => masterOnDayStart(event)
      case event: End => masterOnEnd(event)
      case event: BTrade => masterOnTrade(event)
      case event: Trade => masterOnTrade(event)
      case event: Quote => masterOnQuote(event)
      case event: QuoteFull => masterOnQuoteFull(event)
      case event: QuoteMatch => masterOnQuoteMatch(event)
      case event: Start => masterOnStart(event)
      case event: SingleOrder => masterOnSingleOrder(event)
      case event: Heartbeat => masterOnFIXHeartbeat(event)
      case event: quickfix.fix42.ExecutionReport => masterOnFIXExecutionReport(event)
      case event: MDSInfo => println("mdsinfo")
      case event: DictionaryResponse => println("dictionary response")
      case event: SubscriptionConfirmation => println("subscription confirmation")
      case event: ReplayDateStart => println("replay date start")
      case event: ReplayDateEnd => println("replay day end")
      case event: ReplayConfirmation =>
        println((new Date).toString + "replay confirmation")
        Thread.sleep(5000)
      case _ => println("NO MATCH for: " + event)
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

  def isCrossed: Boolean = {

    if (transType == Quote || transType == QuoteMatch) {
      if (bid >= ask)
        return true
      else return false
    }
    false

//    if (transType == Quote) {
//      if (bidOrAsk == "A") {
//        if (price > bidBefore)
//          return false
//        else  return true
//      }
//      else if (bidOrAsk == "B") {
//        if (askBefore > price)
//          return false
//        else return true
//      }
//      else {
//        println("bidOrAsk undefined")
//        return true
//      }
//    }
//    return false

  }


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
    onDayEnd
  }

  private def masterOnDayStart(dayStart: DayStart) {
    auctionNearFinishTime = lib.combineDateTime(dayStart.dateTime, getEndAuctionClose)
    closeTime = lib.combineDateTime(dayStart.dateTime, getCloseTime)
    openTime = lib.combineDateTime(dayStart.dateTime, getOpenTime)
    date = dayStart.dateTime
    testTime =   lib.combineDateTime(dayStart.dateTime, "15:25:00")
    uncrossTime = lib.combineDateTime(dayStart.dateTime, getUncrossTime)
    onDayStart
  }

  private def masterOnEnd(end: End) {
    onEnd(end)
  }

  private def masterOnFIXExecutionReport(message: quickfix.fix42.ExecutionReport) {

    message.getClOrdID
    message.getOrderID
    val execType = message.getExecType.getValue

    security = message.getSecurityID.toString
    price = message.getPrice.getValue
    volume = message.getOrderQty.getValue.toInt

    execType match {
      case ExecType.NEW => onFIXNew
      case ExecType.PARTIAL_FILL => onFIXPartialFill()
      case ExecType.FILL => onFIXFill()
      case ExecType.CANCELED => print("cancelled")
      case ExecType.REPLACE => print("replace")
      case ExecType.REJECTED => print("rejected")
      case _ => print(execType)
    }

  }

  private def masterOnFIXHeartbeat(heartbeat: Heartbeat) {
    onFIXHeartbeat
  }

  private def masterOnQuote(quote: Quote) {

    transType = "Quote"
    quote.Price /= getCurrencyMultiplier

    date = quote.UpdateTime
    price = (quote.Price)
    volume = quote.Volume
    value = (price * volume)
    security = quote.Security

    // Avoid auctions
    if (quote.UpdateTime.before(closeTime)) {

      // CURRENT
      // *************************************************************

      if (quote.BidOrAsk == 'B') {
        bidOrAsk = "B"
      }
      else if (quote.BidOrAsk == 'A') {
        bidOrAsk = "A"
      }

      onQuote()

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

  private def masterOnQuoteFull(quoteFull: QuoteFull) {
    quoteFull.AskPrice /= getCurrencyMultiplier
    quoteFull.BidPrice /= getCurrencyMultiplier
    security = quoteFull.Security
    price = quoteFull.LastPrice
    highPrice = quoteFull.HighPrice
    lowPrice = quoteFull.LowPrice
    onQuoteFull
  }

  private def masterOnQuoteMatch(quoteMatch: QuoteMatch) {
    transType = "QuoteMatch"
    lastQuoteMatch.put(quoteMatch.Security, quoteMatch)
    quoteMatch.IndicativePrice /= getCurrencyMultiplier
    date = quoteMatch.UpdateTime
    security = quoteMatch.Security
    price = quoteMatch.IndicativePrice
    volume = quoteMatch.MatchVolume
    value = price * volume
    onQuoteMatch()
  }


  private def masterOnSingleOrder(singleOrder: SingleOrder) {

    transType = SingleOrder
    singleOrder.Price /= getCurrencyMultiplier
    date = singleOrder.UpdateTime
    security = singleOrder.Security
    price = singleOrder.Price
    volume = singleOrder.Volume
    value = price * volume
    onSingleOrder()
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
    date = trade.TradeTime
    price = trade.TradePrice
    volume = trade.TradeVolume
    value = (price * volume)

    onBenchmarkTrade
  }

  private def masterOnTrade(trade: Trade) {

    transType = "Trade"
    trade.TradePrice /= getCurrencyMultiplier

    security = trade.Security
    date = trade.TradeTime
    price = trade.TradePrice
    volume = trade.TradeVolume
    value = (price * volume)

    lastTrade.put(security, trade)
    if (!dailyTrades.contains(security)) dailyTrades.put(security, 0)
    if (!dailyValues.contains(security)) dailyValues.put(security, 0)
    val tradeCount = dailyTrades(security) + 1

    dailyTrades.put(security, tradeCount)
    dailyValues.put(security, value)

    onTrade
  }

  def receiveUpdate(event: Any) {
    event match {
      case pump: ObservedEventPump => processEvent(pump.currentEvent)
    }
  }

  /**
   * default stub
   */
  def submitLimitOrder(side: OrderSide, symbol: String, limit: Double, quantity: Int): Order = {
    currentTradesAttemptedToday += 1
    val order = new Order()
    order.setSide(side)
    order.setSymbol(symbol)
    order.setLimit(limit)
    order.setType(OrderType.LIMIT)
    order
  }

  def onBenchmarkTrade() {
    println("onBTrade ")
  }

  def onControl(control: Control) {
    println("onControl: " + control.message)

  }

  def onDayEnd() {
    println("onDayEnd" + date)

    // fudge quotematch objects
  }

  def onDayStart() {
    println("onDayStart" + date)

    // fudge quotematch objects
  }

  def onEnd(end: End) {

  }

  def onFIXFill() {

  }

  def onFIXHeartbeat() {

  }

  def onFIXNew() {

  }

  def onFIXPartialFill() {

  }
  def onQuote() {
    /**
    if (quote.bidOrAsk == 'B') {
      println("onQuote bidBefore" + quote.toString)
    }
    else if (quote.bidOrAsk == 'A') {
      println("onQuote askBefore" + quote.toString)
    }
    else {
      println("onQuote unknown")
    }
     */
  }

  def onQuoteFull() {
  }

  def onQuoteMatch() {

    //quoteMatch.
  }

  def onSingleOrder() {

  }

  def onStart(start: Start) {

  }


  def onTrade() {

  }
>>>>>>> other

}*/