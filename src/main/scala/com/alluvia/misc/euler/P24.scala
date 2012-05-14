package com.alluvia.misc.euler

import com.alluvia.algo.Toolkit

/**
 * /nextdistinct:{$[x~distinct x;:x;.z.s x+1]}
\P 20
n:{$[(10=count distinct "0",string x) & (string x)~distinct string x;:x;x+1]}
ne:{(n/)x}
x: 123456788f
do[1000000;x:ne[x+1]]
x
 */

object P24 extends Toolkit {

  def main(args: Array[String]) {
    var seed = 123456788
    for (i <- 1 to 1000005) {
      seed = nextDistinct(seed+1)
      printcsv("24.csv", seed)
    }
    println(seed)
  }

  def nextDistinct(num: Int): Int = {
    val nextSeq = next(num)
    if (nextSeq == num) return num
    nextDistinct(num + 1)
  }
  def next(num: Int) = {
    val transform = new String(("0" + num).toCharArray.distinct)
    val str = num.toString
    if (transform.length() == 10) num
    else num + 1
  }
}