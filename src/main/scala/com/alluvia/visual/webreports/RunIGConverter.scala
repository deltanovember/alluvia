package com.alluvia.reports

import com.alluvia.markets.{ASX, LSE}
import com.alluvia.visual.webreports.StrategySplitter

object RunIGConverter extends App {

  //println("test")
  //exit(0)
  new StrategySplitter
  new IGConverter with LSE
  new IGConverter with ASX
}