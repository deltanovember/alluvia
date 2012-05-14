package com.alluvia.misc.euler

import com.alluvia.algo.Toolkit
import collection.mutable.{ListBuffer, HashMap, HashSet}
import java.util.Date

object P60 extends App with Toolkit {
  lazy val ps: Stream[Int] = 2 #:: Stream.from(3).filter(i => ps.takeWhile(j => j * j <= i).forall(i % _ > 0))

  println(new Date)
  //val primes = ps.view.takeWhile(_ < 20000).toList
  val allPrimes = ps.view.takeWhile(_ < 10000000).toList
  val primeHash = new HashSet[Int]
  println("primes done", allPrimes.size, new Date)

  allPrimes.foreach(x => primeHash.add(x))

  val chopped = new ListBuffer[List[String]]
  allPrimes.foreach(x => chop(x.toString).foreach(y => chopped += y))
  val primes = chopped.map(x => x.sortBy(y => y)).toList.distinct

  def isPrime(n: String) = n.length() >= 10 || primeHash.contains(n.toInt)

  def chop(num: String) = {
    val combos = new ListBuffer[List[String]]
    val limit = 30000
    for (i <- 1 to num.length() - 1) {
      val first = num.take(i)
      val second = num.takeRight(num.length() - i)
      if (first.toInt < limit && second.toInt < limit &&
        isPrime(first) && isPrime(second))
        combos.append(List(num.take(i), num.takeRight(num.length() - i)))
    }
    combos.toList
  }

  // primes.foreach(println)
  println(primes.length, "*", new Date)
  val twos = primes

  println(twos.length, new Date)
  val threeCandidates = new HashSet[String]
  val threeList = new ListBuffer[List[String]]
  // Each prime has only certain combos
  val twoHash = new HashMap[String, ListBuffer[String]]
  twos.foreach {
    x => if (!twoHash.contains(x(0))) twoHash.put(x(0), new ListBuffer[String])
    twoHash(x(0)) += x(1)
    if (!twoHash.contains(x(1))) twoHash.put(x(1), new ListBuffer[String])
    twoHash(x(1)) += x(0)
  }

  twos.foreach(x => x.foreach(y => threeCandidates.add(y)))
  println(threeCandidates.size, new Date, "%")
  twos.foreach {
    x =>
      twoHash(x(0)).toList.foreach {
        y => if (isPrime(y + x(0)) && isPrime(x(0) + y) && isPrime(y + x(1)) && isPrime(x(1) + y)) threeList += (y :: x)
      }
      twoHash(x(1)).toList.foreach {
        y => if (isPrime(y + x(0)) && isPrime(x(0) + y) && isPrime(y + x(1)) && isPrime(x(1) + y)) threeList += (y :: x)
      }
  }
  val threes = threeList.toList
  println(threes.size, new Date, "!")
  val threeDistinct = threes.map(x => x.sortBy(y => y.toInt)).distinct
  println(threeDistinct.size, new Date, "^")

  val threeHash = new HashMap[String, HashSet[String]]
  threeDistinct.foreach {
    x => if (!threeHash.contains(x(0))) threeHash.put(x(0), new HashSet[String])
    threeHash(x(0)) += x(1)
    threeHash(x(0)) += x(2)
    if (!threeHash.contains(x(1))) threeHash.put(x(1), new HashSet[String])
    threeHash(x(1)) += x(0)
    threeHash(x(1)) += x(2)
    if (!threeHash.contains(x(2))) threeHash.put(x(2), new HashSet[String])
    threeHash(x(2)) += x(0)
    threeHash(x(2)) += x(1)
  }

  val fourCandidates = new HashSet[String]
  val fourList = new ListBuffer[List[String]]
  threeDistinct.foreach(x => x.foreach(fourCandidates.add(_)))
  threeDistinct.foreach {
    x => threeHash(x(0)).foreach {

      y => val list = y :: x
      if (allCombosPrime(list)) fourList.append(list)
    }
    threeHash(x(1)).foreach {

      y => val list = y :: x
      if (allCombosPrime(list)) fourList.append(list)
    }
    threeHash(x(2)).foreach {

      y => val list = y :: x
      if (allCombosPrime(list)) fourList.append(list)
    }
  }
  val fours = fourList.toList
 // fours.foreach(println)
  println(fours.size, new Date, "#")

  val fiveCandidates = new HashSet[String]
  val fiveList = new ListBuffer[List[String]]
  fours.foreach(x => x.foreach(fiveCandidates.add(_)))
  fours.foreach {
    x => fiveCandidates.toList.foreach(y => fiveList.append(y :: x))
  }
  val fives = fiveList.filter {
    x => allCombosPrime(x)
  }
  println(fiveList.size, fiveList(0).size, new Date, "@")
  fives.foreach(println)
  println(fives.size, new Date, "&")

  def allCombosPrime(primes: List[String]) = {
    val combos = primes.combinations(2).toList.map(x => x.toList)
    val filtered = combos.filter(x => isPrime(x(0) + x(1)) && isPrime(x(1) + x(0)))
    combos.length == filtered.length

  }
}