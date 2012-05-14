package com.alluvia.misc.euler

import java.math.BigInteger


object P56 extends App {

  var max = 0
  for (i <- 1 to 99) {
    for (j <- 1 to 99) {
      val exp = new BigInteger(i.toString).pow(j)
      val sum = exp.toString.toCharArray.map(_.toString.toInt).sum
      if (sum > max) max = sum
    }
  }
  println(max)
}