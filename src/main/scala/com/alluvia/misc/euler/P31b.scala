package com.alluvia.misc.euler


object P31b extends App{

  def f(ms: List[Int], n: Int): Int = ms match {
  case h :: t =>
    if (h > n) 0 else if (n == h) 1 else f(ms, n - h) + f(t, n)
  case _ => 0
}

val r = f(List(1, 2, 5, 10, 20, 50, 100, 200), 200)
  println(r)
}