package com.alluvia.visual.webreports

/**
 * Takes raw IG csv files and sorts by strategy
 */

import com.alluvia.algo.Toolkit
import com.alluvia.algo.TypeConverter
import io.Source
import java.io.File
import collection.mutable.{HashMap, HashSet, ListBuffer}
import java.util.Date
import java.text.SimpleDateFormat

class StrategySplitter extends IGBase with TypeConverter with Toolkit {

  val all: List[String] = consolidateCommissions(getAllTransactions())
//  all.foreach(println)
//  exit(0)
  val dir = "data"
  val solo = new ListBuffer[String]
  var mode = "Sunset"

  if (!(new File(dir)).exists()) {
    new File(dir).mkdir()
  }

  // Individual strategies
  val files = new File(".").list().sortBy(name => new java.io.File(name).lastModified()).reverse
  val filtered = files.filter(name => name.contains("Solo.history"))

  val soloTrades = new HashSet[(String, Int)]

  for (file <- filtered) {
    val lines = Source.fromFile(new File(file)).getLines()
    lines.foreach(x => solo += x)
  }

  solo.foreach {
    x => val tokens = x.split(",")
    if (tokens.size > 1) {
      // friday check
      val increment = if (new java.util.Date(tokens(0).getTime).day == 6) 3.days else 1.day
      val date = (new java.util.Date(tokens(0).getTime + increment)).toCustom("dd/MM/yy")
      val price = tokens(2).toDouble
      val volume = tokens(3).toDouble.toInt
      soloTrades.add((date, volume))
    }

  }

  all.foreach {
    x => val tokens = x.split(",")
    val desc = tokens(1)
    val date = tokens(2)
    val volume = tokens(8)
    val price = tokens(9)
    if (desc == "Closing trades" &&
      soloTrades.contains(date, math.abs(volume.toDouble.toInt))) {
      mode = "Solo"
    }
    else if (desc == "Closing trades") {
      mode = "Sunset"
    }
    printcsv("data\\" + mode + ".ig", x)
  //println(x)
  }

  def consolidateCommissions(raw: List[String]): List[String] = {
    // Aggregate identical lines
    var id = 1
    val lines = new HashMap[String, ProfitLine]
    raw.foreach {
      x =>
        val profitLine = getProfitLine(id, x)
        if (lines.contains(profitLine.desc) && profitLine.desc.contains("COMM")) {
          lines(profitLine.desc).amount += profitLine.amount
//          println(true)
//          exit(0)
        }
        else if (profitLine.desc.contains("COMM")) {
          profitLine.id += 2 // move below trade
          lines.put(profitLine.desc, profitLine)
        }
          
        // Roll up dividends
        else if (profitLine.desc.contains("Adjustment for dividend")) {
          
          def toIso(igDate: String) = {

            var parser: SimpleDateFormat = null
            parser = new SimpleDateFormat("dd/MM/yy")
            parser.parse(igDate)
          }
          // Find match
          val fullName = profitLine.desc.split("Adjustment for dividend in ")(1).trim()
          var found = false
          lines.map(x => x._2).toList.sortBy(x => -x.id).foreach {
            y => val date1: Date = toIso(y.date)
            val date2: Date = toIso(profitLine.date)
            if (y.desc.contains(fullName) &&
              !found &&
              date2 < date1) {
              found = true
              y.amount += profitLine.amount
            }
            // else println(x.desc, fullName)
          }
        }
        else lines.put(profitLine.toString, profitLine)
        id += 1
      //println(x, id)
    }
    lines.map(x => x._2).toList.sortBy(x => x.id).map(x => x.toString)
  }
}