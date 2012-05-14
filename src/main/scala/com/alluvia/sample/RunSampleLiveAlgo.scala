package com.alluvia.sample

import com.alluvia.markets.ASX
import java.util.Date
import com.alluvia.algo.datasource.Historical

object RunSampleLiveAlgo {

  def main(args: Array[String]) {
        new Historical with ASX with SampleLiveAlgo {
      val startDate: Date = "2011-06-28"
      val endDate: Date = "2011-06-28"
    } run
  }

}