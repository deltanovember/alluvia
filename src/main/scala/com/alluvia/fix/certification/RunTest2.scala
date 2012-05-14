package com.alluvia.fix.certification

import com.alluvia.markets.LSE


object RunTest2 {
  def main(args: Array[String]) {
    new Test2 with LSE
  }
}