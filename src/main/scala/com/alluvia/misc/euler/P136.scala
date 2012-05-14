package com.alluvia.misc.euler

import collection.mutable.HashMap


object P136 extends App {

  val num = new HashMap[Int, Int]
  for (i <- 1 to 10000) {
    populate(i, 1)
  }
 // populate(13, 1)
  print(num.filter(x => x._2 == 1 && x._1 < 100).keys.size)

  def populate(x: Int, gap: Int) {
    val y = x - gap
    val z = y - gap
    val dif = x*x-y*y-z*z
  // println(dif)
    if (z < 0) return
    else {
      val count = if (num.contains(dif)) num(dif) else 0
      if (dif > 0) num.put(dif, count + 1)
    }
    populate(x, gap + 1)
  }

}