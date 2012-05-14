package com.alluvia.tools

import io.Source

import scala.collection.mutable.HashMap
import com.alluvialtrading.tools.TraderLib
import java.text.SimpleDateFormat
import java.io.FileWriter

object ReutersCrossingFinder {

  val file = "data\\tsx.csv"
  val lib = new TraderLib

  def main(args: Array[String]) {
    val lib = new TraderLib

    var bestBid = 0.0
    var bestAsk = 9999.99

    val bidMap = new HashMap[String, DataLine]
    val askMap = new HashMap[String, DataLine]

    var lastDate = ""

    var lastBid = 0.0
    var lastAsk = 0.0
    var lastBidVolume = 0
    var lastAskVolume = 0


    for (line <- Source.fromFile(file).getLines.drop(1)) {
      val allTokens = line.split(",", -1)

      val RIC = allTokens(0)
      val date = allTokens(1)
      if (date != lastDate) {
        bidMap.clear()
        askMap.clear()
        println("new date" + date)
      }
      val time = allTokens(2)

      if (RIC != "RIM.ALP") {
        // println("debud")
      }

      val bid = if (allTokens(4).length() > 0) allTokens(4).toDouble else 0
      val bidVolume = if (allTokens(5).length() > 0) allTokens(5).toInt else 0
      val ask = if (allTokens(6).length() > 0) allTokens(6).toDouble else 0
      val askVolume = if (allTokens(7).length() > 0) allTokens(7).toInt else 0

      var parser: SimpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS")

      val dateTime: java.util.Date =
        try {
          parser.parse(date + " " + time)
        }
        catch {
          case ex: Exception => {
            ex.printStackTrace
            new java.util.Date
          }
        }


      if (bid > 0 && (bid != lastBid || bidVolume != lastBidVolume)) {
        val bidData = new DataLine(RIC, date, time, bid, bidVolume)
       printcsv("data\\tsxraw.csv", "ENTORD", lib.dateToISODateTimeString(dateTime), RIC, "E",
        bid, bidVolume, bid * bidVolume, "B")
        bidMap.put(RIC, bidData)
        lastBid = bid
        lastBidVolume = bidVolume
      }

      if (ask > 0 && (ask != lastAsk || askVolume != lastAskVolume)) {
        val askData = new DataLine(RIC, date, time, ask, askVolume)
       printcsv("data\\tsxraw.csv", "ENTORD", lib.dateToISODateTimeString(dateTime), RIC, "E",
        ask, askVolume, ask * askVolume, "S")
        askMap.put(RIC, askData)
        lastAsk = ask
        lastAskVolume = askVolume
      }

      val bidList = bidMap.toList sortBy {
        -_._2.price
      }
      val askList = askMap.toList sortBy {
        _._2.price
      }

      if (bidList.length > 3 && askList.length > 3) {
        bestBid = bidList.head._2.price
        bestAsk = askList.head._2.price
      }

      if (bestBid >= bestAsk - 0.0 &&
        bestAsk > 0) {
        println("1. " + bidList.head._2.toString())
        println("2. " + askList.head._2)
      }

      lastDate = date
    }
  }

  def printcsv(fileName: String, args: Any*) {
    val fw = new FileWriter(fileName, true)
    val line = args.mkString(",")
    fw.write(line + "\r\n")
    fw.close()
  }

}


class DataLine(val RIC: String, val date: String, val time: String,
               val price: Double, val volume: Int) {
  override def toString: String = {
    RIC + "," + date + "," + time + "," + price + "," + volume
  }
}