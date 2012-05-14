package com.alluvia.sample.reuters

import com.alluvia.sample.CsvAlgoEvent
import java.util.Date
import com.alluvia.markets.{LSE, ASX}
import com.alluvia.algo.datasource.{Historical, Iress}


object RunSpreadGenerator {
  def main(args: Array[String]) {
        new Historical with ASX with SpreadGenerator {
      val startDate: Date = "2011-01-06"
      val endDate: Date = "2011-01-06"
    } run
  }
}