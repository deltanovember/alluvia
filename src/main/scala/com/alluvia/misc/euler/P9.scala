package com.alluvia.misc.euler

object P9 extends App {

  for (i <- 1 to 1000) {
    for (j <- 1 to 1000) {
      val k = 1000 - i - j
      if (i*i + j*j == k*k) {
        println(i,j,k,i*j*k)

      }
    }
  }
}