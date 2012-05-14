package com.alluvia.misc.euler

import collection.mutable.HashMap
import com.alluvia.types.MagicMap

object P12 extends App {
  var last = 1

  def divides(d: Long, n: Long) = {
    (n % d) == 0
  }

  def ld(n: Long): Long = {
    ldf(2, n)
  }

  def ldf(k: Long, n: Long): Long = {
    if (divides(k, n)) k
    else if ((k * k) > n) n
    else ldf((k + 1), n)
  }

  def factors(n: Long): List[Long] = n match {
    case 1 => Nil;
    case _ => {
      val p = ld(n)
      p :: factors(n / p)
    }
  }

  def countDivisors(num: Long) = {
    (1 to (num).toInt).filter(x => num % x == 0).size
  }

  lazy val triangle: Stream[Int] = 1 #:: Stream.from(2).map {

    p => last = p + last
    last
  }
  // storage
  val triangleStore = new HashMap[Long, Long]

  def getTriangle(number: Long): Long = {
    val solution = if (triangleStore.contains(number - 1)) number + triangleStore(number - 1)
    else {
      val computed = (1L to number).sum
      triangleStore.put(number, computed)
      computed
    }
    solution
  }

val temp = fastCount(6)

  def fastCount(num: Long) = {
    val repeats = MagicMap[Long](0L)
    val primes = factors(num)
  primes.foreach(x => repeats.put(x, 1 + repeats(x)))
    val keys = repeats.values
  (keys.map(_ + 1).foldLeft(1L)((x,y) => x * y))
  }

  var max = 0L
  var counter = 1L
  while (fastCount(getTriangle(counter)) < 500L) {
    max = getTriangle(counter)
    //println(max)
    counter += 1
  }
  //println(fastCount(getTriangle(counter)))
  println(getTriangle(counter))

  //  triangle.takeWhile {
  //    x =>
  //    max = x
  //   // println("!",countDivisors(x))
  //    countDivisors(x) < 5
  //  }.foreach(y =>y)//.foreach(y => println(y, countDivisors(y)))
  //  println("*",max)
}
