package com.alluvia.misc.euler

import collection.mutable.{ListBuffer, HashMap}

object P62 extends App {

  val list = 1 to 10000
  val hash = new HashMap[String, ListBuffer[Int]]
  list.foreach {
    x => val key = ((num: Int) => BigInt(num).pow(3).toString().sortBy(x => x))(x)
    if (!hash.contains(key)) hash.put(key, new ListBuffer[Int])
    hash(key) += x
    if (hash(key).size == 5) println(BigInt(hash(key).min).pow(3))
  }
}