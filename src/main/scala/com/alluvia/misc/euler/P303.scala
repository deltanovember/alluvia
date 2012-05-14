package com.alluvia.misc.euler

object P303 extends App {

  //println((1 to 1000).map(x => f(BigInt(x), BigInt(x))/x).sum)
  println(f(BigInt(999), BigInt(999)))
 // (1 to 1000).foreach(x => println(x, f(BigInt(x), BigInt(x))))
  def f(n: BigInt, orig: BigInt): BigInt = {
    def lessThan(big: BigInt) = big.toString().sortBy(x => x).takeRight(1) < "3"
    if (lessThan(n)) return n
    println(n)
    return f(n + orig, orig)
  }
}