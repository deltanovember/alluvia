package com.alluvia.misc.euler


object P160 extends App {

  println(fact(BigInt("1000000000000")))
  def fact(num: BigInt): BigInt = {
    def helper(num: BigInt, acc: BigInt): BigInt = {
      if (num == BigInt(1)) return acc
      else helper(num-1,acc*num)
    }
    helper(num, 1)
  }
}