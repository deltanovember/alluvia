package com.alluvia.misc.euler

object P55 extends App {

  def pal(num: BigInt) = num.toString == num.toString.reverse
  def lychrel(num: BigInt, counter: Int): Boolean = {
    if (50 == counter) {
     // println(num)
      return true
    }
    else if (pal(num))return  false
    lychrel(num + BigInt(num.toString().reverse), counter+1)
  }
  val l = 1 to 9999
  val sol = l.map(x => BigInt(x)).filter(x => lychrel(x+BigInt(x.toString().reverse), 1))
  println("*", sol.length)
  sol.foreach(println)
 // println(notLychrel(BigInt(47), 0))

}