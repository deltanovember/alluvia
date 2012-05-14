package com.alluvia.misc.euler

import java.math.{MathContext, BigInteger}

object P26 extends App {

  def countRepeats(num: BigDecimal, count: Int): Int = {
    val decimalsFull = num.toString().split("\\.")(1)
     if (decimalsFull.length() < 10) return decimalsFull.length()
    val decimals = decimalsFull.substring(0, decimalsFull.length()-2)
    if (count >= decimals.length()) return count

    val len = decimals.length()
    val first = decimals.substring(len - count, len)
    val second = decimals.substring(len - 2*count, len-count)

    if (first == second) return count
    countRepeats(num, count+1)
  }
  val mc = new MathContext(20480);
  val num = BigDecimal(1.0, mc)
  val den = BigDecimal(19.0, mc)
  println(countRepeats(num/den, 1))
 val max = (1 to 1000).map(x => BigDecimal(x, mc)).map(x => (x, countRepeats(num/x, 1))).maxBy(x => x._2)
  println(max._1, max._2)

}