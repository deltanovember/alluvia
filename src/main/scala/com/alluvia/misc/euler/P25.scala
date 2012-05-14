package com.alluvia.misc.euler

import java.math.BigInteger


object P25 extends App {

  var counter = 1
  println(getFib(new BigInteger("1"), new BigInteger("1")))

  def getFib(x: BigInteger, y: BigInteger): BigInteger = {
    counter +=1
    if (y.toString.length() == 1000)
      return y
    getFib(y, x.add(y))

  }
  println(counter)
}