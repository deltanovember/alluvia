package com.alluvia.tools.dataquality

import com.alluvia.markets.ASX
import java.util.Date
import com.alluvia.algo.datasource.{Iress, IressReplay, Historical}

object RunEventStreamer {

  def main(args: Array[String]) {

    new IressReplay with ASX with EventStreamer {
      val startDate: Date = "2011-11-16"
      val endDate: Date = "2011-11-16"
    } run

  }

}