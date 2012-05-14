package com.alluvia.fix.certification

import com.alluvia.markets.{ASX, LSE}

object RunTest10 {
  def main(args: Array[String]) {
    new Test10 with ASX
  }
}