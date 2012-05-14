package com.alluvia.tools.kaggle

import io.Source
import java.text.{NumberFormat, ParsePosition}
import com.alluvia.algo.{TypeConverter, Toolkit}
import java.util.Date
import collection.mutable.{ListBuffer, HashMap}
import com.alluvia.types.MagicMap

object Audit extends Toolkit with TypeConverter {

  // Parameters
  val directory = "data"
  val testing = "testing.csv"
  val minTime = 5.minutes
  val lastEvent = MagicMap[(String, String)](new ListBuffer[Date])
  val startTime = lib.combineDateTime(new Date, "08:00:00.000")
  val endTime = lib.combineDateTime(new Date, "16:30:00.000")
  val startRow = 754019

  def main(args: Array[String]) {

    auditTest(directory + "\\" + testing)

    def auditTest(file: String) {
      var row = 0
      // first data line
      for (line <- Source.fromFile(file).getLines.drop(1).take(1)) {
        if ("754019" != line.split(",")(0)) println("Bad start row")
      }
      for (line <- Source.fromFile(file).getLines.drop(1)) {

        row += 1

        if (line.trim().length < 10) println("blank line")
        else {
          val tokens = line.split(",").toList
          if (row > 1 && (row + startRow - 1).toString != tokens(0)) println("Invalid row", tokens(0), row + startRow - 1)
          val security = tokens(1)
          val count = tokens(2)
          val firstTime = lib.combineDateTime(new Date, tokens(8))

          lastEvent(security, count) += firstTime
          if (tokens.length != 207) println("Wrong number of columns: " + file)
          if (tokens.filter(x => x.equals("0")).length > 0) println("zero line")
          tokens.drop(6).foreach {
            x => if (isNumeric(x) && (x.toDouble < 0.0001 || x.toDouble > 50000)) println("Wrong range", x)
            if (x.contains(":")) {
              val time = lib.combineDateTime(new Date, x)
              if (time < startTime || time > endTime) println("Invalid time")
            }

          }
        }
      }

      def tooClose(list: ListBuffer[Date]) = {
        var min = 24.hours
        val sorted = list.sortBy(x => x.getTime)
        for (i <- 0 to sorted.length - 2) {
          if (0 == i) min = sorted(i + 1) - sorted(i)
          if (sorted(i + 1) - sorted(i) < min) {
            min = sorted(i + 1) - sorted(i)
          }
                      if (min < minTime) {
             // println(sorted(i + 1), sorted(i))
            }
        }

        if (min < minTime) {
          //println("debug")
        }
        min < minTime
      }
      // Gap check
      lastEvent.foreach {
        x => if (tooClose(x._2)) println("too close")
      }

      // Time check
      if (50001 != row) println("Incorrect rows")
    }


    def auditFile(file: String) {
      var row = 0
      for (line <- Source.fromFile(file).getLines) {
        row += 1
        if (line.trim().length < 10) println("blank line")
        else {
          val tokens = line.split(",").toList
          if (tokens.filter(x => x.equals("0")).length > 0) println("zero line")
        }
      }
      println(file, row)
    }
  }

}