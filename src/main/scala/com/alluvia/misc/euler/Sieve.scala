package com.alluvia.misc.euler

/**
 * Sieve of Eratosthenes
 */

class Sieve {

  val max = 775146
  def filterPrimes(seed: Int = 2, sieve: List[Int] = List()): List[Int] =
    sieve.filter(x => x <= seed || x % seed != 0)

  var filtered: List[Int] = (2 to max).toList
  for (i <- 2 to scala.math.sqrt(max).toInt) filtered = filterPrimes(i, filtered)
  filtered.filter(x => 600851475143L % x == 0).foreach(println(_))
  //filtered.filter(x => max % x == 0).foreach(println(_))
}

object RunSieve {
  def main(args: Array[String]) {
      println("starting")
  new Sieve
  }

}