package com.alluvia.sample

import com.alluvia.algo.EventAlgo
import java.util.Date
import scala.collection.mutable.HashMap
import com.alluvia.types.market.{DayEnd, End, Quote}

abstract class CrossingEventAlgo(val startDate: Date, val endDate: Date) extends EventAlgo {

  val bidMap = new HashMap[String, Quote]
  val askMap = new HashMap[String, Quote]
  val crossingMap = new HashMap[String, CrossingInterval]

  var bestBid = 0.0
  var bestAsk = 9999.99


  override def onDayEnd(d: DayEnd) {
    //println(date)
    bidMap.clear()
    askMap.clear()
  }

  override def onEnd(end: End) {
    println("END")
    val sorted = crossingMap.toList.sortBy((_._2.getInterval))
  sorted.toList.foreach(x => println(x._1 + "," + x._2.getInterval + "ms"))
   // crossingMap.keySet.toList.sortBy(_.).foreach(println(_ ))
  }

  override def onQuote(q: Quote) {
    /**
    // println(quote)
    if (quote.BidOrAsk == 'B') {

      if (quote.Price > 0 &&
        quote.UpdateTime.toTimeStr >= "09:31" &&
        quote.UpdateTime.toTimeStr < "16:30") {
        bidMap.put(quote.Security, quote)
      }
    }
    else if (quote.BidOrAsk == 'A') {
      if (quote.Price > 0 &&
        quote.UpdateTime.toTimeStr >= "09:31" &&
        quote.UpdateTime.toTimeStr < "16:30") {
        askMap.put(quote.Security, quote)
      }
    }
    else {
      println("onQuote unknown")
    }

    val bidList = bidMap.toList sortBy {
      -_._2.Price
    }
    val askList = askMap.toList sortBy {
      _._2.Price
    }

    if (bidList.length > 0 && askList.length > 0) {
      bestBid = bidList.head._2.Price
      bestAsk = askList.head._2.Price
    }

    if (bestBid > bestAsk && bestAsk > 0) {
      val q1 = bidList.head._2
      val q2 = askList.head._2
      val crossingData = q1.Security + "," + q1.UpdateTime + "," + q1.Price + "," + q1.Volume + "," +
        q2.Security + "," + q2.UpdateTime + "," + q2.Price + "," + q2.Volume
      //println(crossingData)
      if (crossingMap.contains(crossingData)) {
        val d1 = crossingMap(crossingData).date1
        val d2 = quote.UpdateTime
        crossingMap.put(crossingData, new CrossingInterval(d1, d2))
        //println("updating" + crossingMap(crossingData).getInterval + d1.toString + d2 + "here")
       //System.exit(0)
      }
      else {
        crossingMap.put(crossingData, new CrossingInterval(q1.UpdateTime, q1.UpdateTime))
      }


      if (bestBid - bestAsk > 0.1) {
        println("abnormal")
        System.exit(0)
      }
    }

*/
  }

}

class CrossingInterval(val date1: Date, val date2: Date) {
  def getInterval = date2.getTime - date1.getTime
}