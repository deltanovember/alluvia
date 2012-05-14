package com.alluvia.tools.exchange

import collection.mutable.HashMap
import com.alluvia.types.market.{QuoteMatch, SingleOrder}
import java.util.Date

/**
 * Represent exchange order book
 */

class OrderBook {

  type Price = Double
  type Volume = Double
  type Surplus = Double
  val book = new HashMap[Price, Volume]
  val orders = new HashMap[Long, SingleOrder]

  def addOrder(singleOrder: SingleOrder) {

    if (singleOrder.price == 1.35 && singleOrder.bidOrAsk == 'A')
      println("debug")
    // Raw
    orders.put(singleOrder.orderNo, singleOrder)

    // ASK = negative price
    val price = if (singleOrder.bidOrAsk == 'B') singleOrder.price else -singleOrder.price
    if (!book.contains(price)) book.put(price, singleOrder.volume)
    else {
      book.put(price, singleOrder.volume + book(price))
    }
        //  println(singleOrder.date, price, singleOrder.volume)
  }

  def deleteOrder(singleOrder: SingleOrder) {

    // Raw
    orders.remove(singleOrder.orderNo)

    val price = if (singleOrder.bidOrAsk == 'B') singleOrder.price
    else -singleOrder.price
    if (book.contains(price))
      book.put(price, -singleOrder.volume + book(price))
    else println("Cannot delete order", singleOrder.security, singleOrder.orderNo)
  }

  //  0.43,0      |  9999
  // 0.425,10000  | 4243
  // 0.42,543     | 3372
  // 0.415,926    | 0

  def displayBook() {

    def pad(price: Double, volume: Double): String = {
      val length = 12
      var combined = price + "," + volume
      for (i <- 0 to length - combined.length()) combined += " "
      combined

    }
    // Retrieve all prices
    val prices = book.keys.map(x => math.abs(x)).toList.distinct.sortBy(x => -x)
    prices.foreach {
      x => val bidVolume = if (book.contains(x)) book(x) else 0.0
      val askVolume = if (book.contains(-x)) book(-x) else 0.0
        println(pad(x, bidVolume) + "\t|\t" + askVolume)
    }
  }

  def getCumulativeBook(book: HashMap[Double, Double], sortedKeys: List[Double]) = {

    val accum = new HashMap[Double, Double]
    accum.put(sortedKeys.head, book(sortedKeys.head))
    for (i <- 1 to sortedKeys.length - 1) {
      accum.put(sortedKeys(i), book(sortedKeys(i)) + accum(sortedKeys(i - 1)))
    }
    accum
  }

  def getQuoteMatch = {

    val bidPrices: List[Double] = book.keys.filter(_ > 0).toList.sortBy(x => -x)
    val askPrices = book.keys.filter(_ < 0).toList.sortBy(x => -x)

    val bidCumulative = getCumulativeBook(book, bidPrices)
    val askCumulative = getCumulativeBook(book, askPrices)
    //askCumulative.foreach(x => println(x._1, x._2))

    // Match volume at each level
    val uncross = new HashMap[Price, (Price, Volume, Surplus)]
    bidCumulative.keys.foreach {
      x => val bidTotal = bidCumulative(x)
      val askTotal = if (askCumulative.contains(-x)) askCumulative(-x) else 0
      if (bidTotal > askTotal) uncross.put(x, (x, askTotal, bidTotal - askTotal))
      else if (askTotal > bidTotal) uncross.put(x, (x, bidTotal, bidTotal - askTotal))
      else println("Exact match")
    }

    val matchLevel = uncross.values.toList.sortBy(x => -x._2).head
    QuoteMatch(new Date, "", matchLevel._1, matchLevel._2, matchLevel._1 * matchLevel._2, 0.0, 0.0, matchLevel._3)
  }

  def modifyOrder(singleOrder: SingleOrder) {
    val current = orders(singleOrder.orderNo)
    deleteOrder(current)
    addOrder(singleOrder)
  }
}