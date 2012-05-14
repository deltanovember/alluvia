package com.alluvia.sample.demo

import com.alluvia.algo.EventAlgo
import com.alluvia.sample.Benchmark
import com.alluvia.markets.ASX
import java.util.Date
import com.alluvia.algo.datasource.{Iress, Historical, Smarts}

trait PaulAlgo extends EventAlgo {

}

object RunPaulAlgo {
    def main(args: Array[String]) {
    new Iress with PaulAlgo with ASX  {
      val startDate: Date = "2011-09-14"
      val endDate: Date = "2011-09-14"    } run

  }
}