package com.alluvia.sample

import com.alluvia.markets.ASX
import java.util.Date
import com.alluvia.algo.datasource.{Smarts, Historical, IressReplay}

object RunBenchmark {
    def main(args: Array[String]) {
    new Smarts with Benchmark with ASX  {
      val startDate: Date = "2011-09-14"
      val endDate: Date = "2011-09-14"    } run

  }
}