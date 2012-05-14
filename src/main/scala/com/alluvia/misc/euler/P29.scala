package com.alluvia.misc.euler

import java.math.MathContext
import collection.mutable.HashSet


object P29 extends App {

  val mc = (MathContext.UNLIMITED);
  val distinct = new HashSet[String]
  for (i <- 2 to 100) {
    for (j <- 2 to 100) {
      distinct.add(BigDecimal(i, mc).pow(j).toString())
    }
  }
  println(distinct.size)
}