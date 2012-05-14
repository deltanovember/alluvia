package com.alluvia.misc.euler

import collection.mutable.HashSet


object P39 extends App {

  def pythag(num: Int) = {
    val sides = new HashSet[(Int, Int, Int)]
    for (i <- 1 to  num) {
      for (j <- 1 to num) {
        for (k <- 1 to num) {
          if (i*i+j*j==k*k && i+j+k==num) sides.add((i, j, k))
        }
      }
    }
    sides.toList
  }

  //val sol = pythag(900)
 // println(sol)
  val all = 1 to 1000
 println(all.map(x => (x, pythag(x).size)).maxBy(x => x._2))
}