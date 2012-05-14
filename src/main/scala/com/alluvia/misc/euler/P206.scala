package com.alluvia.misc.euler


object P206 extends App {

  for (i <- 0 to 1000000000) {
    val sq = BigInt(i) * BigInt(i)
    val str = sq.toString()
    if (str.length() == 19) {
      if (str(0) == '1' &&
        str(0) == '2' &&
        str(0) == '3' &&
        str(0) == '4' &&
        str(0) == '5' &&
        str(0) == '6' &&
        str(0) == '7' &&
        str(0) == '8' &&
        str(0) == '9' &&
        str(0) == '0'
      ) {
        print(i)
        exit(0)
      }
    }
  }

}