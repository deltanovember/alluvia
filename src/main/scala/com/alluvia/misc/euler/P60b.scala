package com.alluvia.misc.euler

import collection.mutable.HashSet

object P60b {

  def main(args: Array[String]) {
    lazy val ps: Stream[Int] = 2 #:: Stream.from(3).filter(i => ps.takeWhile(j => j * j <= i).forall(i % _ > 0))

    val primeString = new HashSet[String]
    ps.view.takeWhile(_ < 9999).toList.foreach(x => primeString.add(x.toString))
    val ordered = primeString.toList.sortBy(x => x.toInt)
    findCombo(List(ordered.head), ordered.tail, ordered.tail).foreach(println)

    def isPrime(n: String) = primeString.contains(n)


    def findCombo(combo: List[String], all: List[String], orig: List[String]): List[String] = {

      def max(l1: List[String], l2: List[String]) = if (l1.length > l2.length) l1 else l2


      if (combo.length == 4) return combo
      if (all.length == 0) return combo
      if (!allCombosPrime(combo)) return combo.drop(1)
      return max(findCombo(all.head :: combo, all.tail, orig), findCombo(combo, all.tail, orig))
      //var potential = all.head :: combo

     // if (!allCombosPrime(potential)) return findCombo(combo, all.tail, orig)



//      val newCombo = if (allCombosPrime(potential)) potential else combo
//      val newAll = if (all.length == 1) orig.tail else all.tail
//      val newOrig = if (all.length == 1) orig.tail else orig
//      return findCombo(newCombo, newAll, newOrig)
    }

    def allCombosPrime(primes: List[String]) = {
      val combos = primes.combinations(2).toList.map(x => x.toList)
      val filtered = combos.filter(x => isPrime(x(0) + x(1)) && isPrime(x(1) + x(0)))
      combos.length == filtered.length

    }

  }
}