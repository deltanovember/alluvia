package com.alluvia.visual.spread2

import java.util.Date
import com.alluvia.algo.datasource.{Historical, IressReplay}
import com.alluvia.markets.{ASX, LSE}

object RunSpreadGenerator2 {

  def main(args: Array[String]) {

    new IressReplay with ASX with SpreadGenerator2 {
      val startDate: Date = "2011-11-28"
      val endDate: Date = "2011-11-28"
    } run

  }

}