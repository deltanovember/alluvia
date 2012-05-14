package com.alluvia.misc.euler

import collection.mutable.ListBuffer

object P4 extends App {

  val range = 1 to 999
  val palindromes = new ListBuffer[Int]
  range.foreach {
    x => (1 to 999).map(y => y * x).filter(z => isPalindrome(z.toString)).foreach(a => palindromes.append(a))
  }
println(palindromes.max)
  
  def isPalindrome(number: String) = number.reverse == number
}