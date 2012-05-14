package com.alluvia.misc.euler

import collection.mutable.ListBuffer


object P361 {

  def main(args: Array[String]) {

    def tm(index: Int): Int = {
      if (0 == index) return 0
      else if (index % 2 == 0) return tm(index/2)
      else return 1-tm((index-1)/2)
    }
    def tdirect(index: BigInt):Int = {
      val bin = tobin(index)
      val ones = bin.filter(x => x== '1').sum

      if (ones % 2 == 0) return 0
      else return 1
    }

    def tobin(n: BigInt): String = {
      if(n==0 || n== 1) {
        return n.toString
      }
      else
        return tobin(n/2)+(n.mod(BigInt(2)))
    }

    def member(bin: String): Boolean = {
      if (bin.length() < 3) return true
      val sub = new ListBuffer[String]
      for (i <- 0 to bin.length() / 2) sub += bin.substring(0, i)
      var mem = sub.map(x => if (bin.contains(x + x + x.charAt(0))) true else false).filter(x => x).length == 1
      mem
    }

//    def a(num: BigInt, seq: String, end: BigInt): BigInt = {
//      val bin = tobin(num)
//      if (bin.toString == seq) return end
//
//      val newseq = seq.substring(1, seq.length()) + tdirect(end+1)
//      return a(num, newseq, end+1)
//    }

    var start = BigInt("0")
    val end = BigInt("18")
    var a = BigInt("0")
    while (start != end) {
      if (member(tobin(start))) a += 1
      start += 1
    }
    println(a)

//    val num = BigInt("13")
//    println(tobin(num))
//     val seq = (0 to tobin(num).length()-1).map(x => tdirect(BigInt(x)).toString).reduceLeft(_ + _)
//    println(a(num, seq, tobin(num).length()))
   // println(tdirect(BigInt(3)))
//    val myseq = 1 to 18
//    val conv = myseq.map(x =>BigInt(10).pow(x))
//    println(tobin(conv.sum))
//    println(conv.sum)
//    val test = 0 to 20
//    test.map(x => tdirect(x)).foreach(print)
//    println("*",tobin(4))
//    println(tobin(BigInt(100)))
//    println(tdirect(BigInt("80852364491")))
  }
}