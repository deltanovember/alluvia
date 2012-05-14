package com.alluvia.misc.euler

import math.BigInt._


object P97 extends App {
  println((BigInt(28433)*power(1, 7830457)+BigInt(1)).toString().takeRight(10))
  def power(total: BigInt, pow: Int): BigInt = {
    if (0 == pow) return total
    power(tail(total * BigInt(2)), pow - 1)
  }
  def tail(num: BigInt) = BigInt(num.toString().takeRight(15))
}