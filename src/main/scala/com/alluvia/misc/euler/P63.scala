package com.alluvia.misc.euler

object P63 extends App{

  val max = 100
  var count = 0
  for (i <- 1 to max) {
    for (j <- 1 to max) {
      if (isPower(i, j)) count += 1
    }
  }

  println(count)
  def isPower(num: Int, pow: Int) = {
    val big = BigInt(num)
    big.pow(pow).toString().length() == pow
  }
}