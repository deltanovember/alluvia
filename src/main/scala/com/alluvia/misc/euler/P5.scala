package com.alluvia.misc.euler


object P5 extends App {

  class RichStream[A](str: => Stream[A]) {
    def ::(hd: A) = Stream.cons(hd, str)
  }

  implicit def streamToRichStream[A](str: => Stream[A]) = new RichStream(str)
    def from(n: Int): Stream[Int] = {
    println("Requesting n = " + n)
    n :: from(n + 1)
  }

  def smallest(seed: Int): Int = {
     val testList:List[Int] = List(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20)
    if (testList.filter(x => seed % x == 0).size == testList.size) seed
    else smallest(seed + 20)

  }
  println(smallest(20))
}