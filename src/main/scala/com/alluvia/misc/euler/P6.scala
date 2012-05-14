package com.alluvia.misc.euler

object P6 extends App {

  def sumSquares(end: Int) = (1 until end + 1).map(x => x * x).sum
  def squareSums(end: Int) = (1 until end + 1).sum * (1 until end + 1).sum

  println(squareSums(100) - sumSquares(100))
}