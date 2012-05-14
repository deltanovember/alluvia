package com.alluvia.misc.euler

import java.math.BigInteger


object P48 extends App {
  var sum = new BigInteger("0")
  val list = (1 to 1000).map(x => new BigInteger(x.toString).pow(x))
  list.foreach(x => sum = sum.add(x))
  println(sum)
}