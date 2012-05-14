package com.alluvia.misc.euler


object P7 extends App {
  lazy val ps: Stream[Int] = 2 #:: Stream.from(3).filter(i =>
    ps.takeWhile(j => j * j <= i).forall(i % _ > 0))
  var row = 0
  ps.takeWhile(x => row <= 10001).foreach {
    x =>
      row += 1
      println(row + "," + x)
  }
}