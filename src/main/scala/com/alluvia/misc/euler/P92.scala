package com.alluvia.misc.euler

object P92 extends App {

  val list = 1 to 9999999
  println(list.map(x => getLast(x)).filter(x => x == 89).size)
  def getLast(num: Int): Int = {
    if (num == 1 || num == 89) return num
    val sq = num.toString.map(x => x.toString.toInt).map(x => x*x).sum
    getLast(sq)
  }
}