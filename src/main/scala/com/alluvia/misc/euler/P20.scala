package com.alluvia.misc.euler

import java.math.BigInteger

object P20 extends App {

  def fact(i: BigInteger):BigInteger = {
    if (i.equals(new BigInteger("0")))
      new BigInteger("1")
      else i.multiply(fact(i.subtract(new BigInteger("1"))))
  }
  println(fact(new BigInteger("100")).toString.toCharArray.map(x => x.toString.toInt).sum)
}