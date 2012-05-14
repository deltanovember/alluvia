package com.alluvia.misc.euler

object P17 extends App {

  val toWord = Map(
  1 -> "one", 2 -> "two", 3 -> "three", 4 -> "four", 5 -> "five", 6 -> "six", 7 -> "seven",
  8 -> "eight", 9 -> "nine", 10 -> "ten", 11 -> "eleven", 12 -> "twelve", 13 -> "thirteen",
  14 -> "fourteen", 15 -> "fifteen", 16 -> "sixteen", 17 -> "seventeen", 18 -> "eighteen",
  19 -> "nineteen", 20 -> "twenty", 30 -> "thirty", 40 -> "forty", 50 -> "fifty", 60 -> "sixty",
  70 -> "seventy", 80 -> "eighty", 90 -> "ninety"
  )

  def translate(num: Int): String = {
    if (num < 20) toWord(num)
          else if (num == 1000) {
      "one thousand"
    }
    else if (num <= 90 && num % 10 == 0) toWord(num)
    else if (num < 100) {
      val head = 10 * (num / 10)
      toWord(head) + " " + toWord(num - head)
    }
    else if (num % 100 == 0) {
            val head = 100 * (num / 100)
      toWord(num / 100) + " hundred"
    }

    else {
                  val head = 100 * (num / 100)
      toWord(num / 100) + " hundred and " + translate(num - head)
    }
  }

  (1 to 1000).foreach(x => println(translate(x)))
  print((1 to 1000).map(x => translate(x).replaceAll(" ", "").length()).sum)
}