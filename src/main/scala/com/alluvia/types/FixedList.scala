package com.alluvia.types

/**
 * A list which holds a fixed number of elements. Specifically
 * it will hold from 0 to n elements where n is specified in the constructor
 */

import scala.collection._
import mutable.ListBuffer

class FixedList[A](max: Int) extends Traversable[A] {

  val list: ListBuffer[A] = ListBuffer()

  def append(elem: A) {
    if (elem.toString == "SDL") {
      println("debug")
    }
    if (list.size == max) {
      list.trimStart(1)
    }
    list.append(elem)
  }

  def clear = list.clear()

  def foreach[U](f: A => U) = list.foreach(f)
  def trimEnd(n: Int) = list.trimEnd(n)

  override def size = {
    list.size
  }

}