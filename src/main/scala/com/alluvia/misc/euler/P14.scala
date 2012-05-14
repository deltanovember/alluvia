package com.alluvia.misc.euler

object P14 extends App {

  def collatz(seed: Long): List[Long] = {
    def collatzHelper(seed: Long, acc: List[Long]): List[Long] = {
      if (seed == 1) 1::acc
      else {
        //println(seed)
        val next = if (seed % 2 == 0) seed / 2 else 3 * seed + 1
        collatzHelper(next, seed::acc)
      }
    }
    collatzHelper(seed, List()).reverse
  }

  var max = 0
  var maxItem = 0
  for (i <- 1 to 999999) {
    val listLength = collatz(i).length
    if (listLength > max) {
      max = listLength
      maxItem = i
    }
  }
  println(maxItem)
  //println((1 to 999999).sortBy(x => collatz(x).length).last)
  //print(collatz(13).length)


}