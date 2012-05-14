package com.alluvia.fix.certification

import com.alluvia.markets.LSE


object RunTest1 {
  def main(args: Array[String]) {
    new Test1 with LSE
  }
}