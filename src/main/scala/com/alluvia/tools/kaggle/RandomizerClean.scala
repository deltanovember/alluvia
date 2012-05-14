package com.alluvia.tools.kaggle

import io.Source
import com.alluvia.algo.Toolkit
import com.alluvia.algo.TypeConverter
import collection.mutable.{HashMap, ListBuffer}
import java.util.Date

/**
 * Enforce clean windows
 */
object RandomizerClean extends Toolkit with TypeConverter {
  def main(args: Array[String]) {

    var row = 0
    val data = new ListBuffer[String]
    val lastEvent = new HashMap[String, Date]
    val lastRow = new HashMap[String, String]
    val window: TimeSpan = 10.minutes
    val dir = "data"
    val in = dir + "\\" + "testing_in_all.csv"
    val out = dir + "\\" + "clean_window.csv"

    for (line <- Source.fromFile(in).getLines.take(1)) {
      printcsv(out, line)
    }
    // Generate all clean windows
    for (line <- Source.fromFile(in).getLines.drop(1)) {
      val tokens = line.split(",")
      val security = tokens(1)
      val time = tokens(203)
      val date: java.util.Date = lib.combineDateTime(new Date(), time)
      // fresh event or clean window event or new day rollover
      if (!lastEvent.contains(security) || (lastEvent.contains(security) && (date - window > lastEvent(security)))) {
        lastEvent.put(security, date)
        lastRow.put(security, line)
        data += line
        printcsv(out, line)
      }
      else if (lastEvent.contains(security)) {
        if (lastEvent(security) < date) {
                  printcsv(dir + "\\dirty.csv", lastRow(security))
        printcsv(dir + "\\dirty.csv", line)
        }

      }

    }

    val randomized = data.toArray
    java.util.Collections.shuffle(java.util.Arrays.asList(randomized: _*))
    for (i <- 0 to 49999) printcsv("testing_clean_window.csv", (row + i) + "," + randomized(i))
  }


}