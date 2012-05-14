package com.alluvia.misc.euler

import collection.mutable.HashSet


object P60c extends App {

  lazy val ps: Stream[Int] = 2 #:: Stream.from(3).filter(i => ps.takeWhile(j => j * j <= i).forall(i % _ > 0))

  val primeString = new HashSet[String]
  val primes = ps.view.takeWhile(_ < 9999).toList
  println(primes(0))

  for (a <- 0 to primes.length - 1) {
    for (b <- a to primes.length - 1) {

      if (allCombosPrime(List(primes(a).toString, primes(b).toString))) {
        for (c <- b to primes.length - 1) {
          if (allCombosPrime(List(primes(c).toString, primes(b).toString)) &&
            allCombosPrime(List(primes(c).toString, primes(a).toString))) {
            for (d <- c to primes.length - 1) {
              if (allCombosPrime(List(primes(a).toString, primes(d).toString)) &&
                allCombosPrime(List(primes(d).toString, primes(b).toString)) &&
                allCombosPrime(List(primes(d).toString, primes(c).toString))) {
                //println(primes(a), primes(b), primes(c), primes(d))
                for (e <- d to primes.length - 1) {
                  if (allCombosPrime(List(primes(a).toString, primes(e).toString)) &&
                    allCombosPrime(List(primes(e).toString, primes(b).toString)) &&
                    allCombosPrime(List(primes(e).toString, primes(c).toString)) &&
                    allCombosPrime(List(primes(e).toString, primes(d).toString)))
                    println(primes(a), primes(b), primes(c), primes(d), primes(e), primes(a) + primes(b) + primes(c) + primes(d) + primes(e))

                }

              }
            }
          }
        }
      }
    }
  }

  def allCombosPrime(primes: List[String]) = {
    val combos = primes.combinations(2).toList.map(x => x.toList)
    val filtered = combos.filter(x => isPrime(x(0) + x(1)) && isPrime(x(1) + x(0)))
    combos.length == filtered.length

  }

  //checks whether an int is prime or not.
  def isPrime(numString: String): Boolean = {
    val n = numString.toInt
    //check if n is a multiple of 2
    if (n % 2 == 0) return false;
    if (n <= 1) return false
    //if not, then just check the odds
    val ceil = scala.math.sqrt(n).toInt + 1
    for (i <- 3 to ceil by 2) {
      if (n % i == 0)
        return false
    }

    return true
  }

  def isPrime(num: Int): Boolean = isPrime(num.toString)

}