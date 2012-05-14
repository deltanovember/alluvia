package com.alluvia.sample

import com.alluvia.markets.ASX

object RunSample {

  def main(args: Array[String]) {
   new SampleAlgo with ASX run
  }
}