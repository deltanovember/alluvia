package com.alluvia.tools.greencross

import io.Source
import com.alluvia.algo.Toolkit
import com.alluvia.algo.TypeConverter
import collection.mutable.HashMap
import java.util.Date

object ReportGenerator extends App with Toolkit with TypeConverter {

  //case class Line()
  val file = "backtesting_gcc.csv"
  printcsv(file, "Date", "Gross", "Net", "Margin")
  val consolidated = new HashMap[String, (Int, Double, Double)]
  for (line <- Source.fromFile("backtesting.csv").getLines) {
    val tokens = line.split(",")
    val date = tokens(0)
    val profit = tokens(10).toDouble
    val brokerage = tokens(8).toDouble
    if (consolidated.contains(date)) {
      val existing = consolidated(date)
      consolidated.put(date, (1+existing._1, profit + existing._2, brokerage + existing._3))
    }
    else
      consolidated.put(date, (1, profit, brokerage))


  }
     // println(consolidated.size)
    consolidated.keys.toList.sortBy(x => x).foreach {
      x =>
      val profit = consolidated(x)._2
      val brokerage = consolidated(x)._3
      val turnover = consolidated(x)._3*1000/2
      val date: Date = x
      printcsv(file, date.toCustom("dd-MM-yyyy"), BigDecimal(round((profit+brokerage)/turnover,6)), BigDecimal(round((profit)/turnover,6)), 0.0)

    }
}