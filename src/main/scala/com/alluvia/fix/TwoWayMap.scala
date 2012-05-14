package com.alluvia.fix

import java.util.HashMap

class TwoWayMap {
  val firstToSecond = new HashMap[Any, Any]
  val secondToFirst = new HashMap[Any, Any]

  def put(first: Any, second: Any) {
    firstToSecond.put(first, second);
    secondToFirst.put(second, first);
  }

  def getFirst(first: Any): Any = {
    return firstToSecond.get (first);
  }

  def getSecond(second: Any): Any = {
    return secondToFirst.get (second);
  }

  override def toString(): String = {
    firstToSecond.toString() + "\n" + secondToFirst.toString();
  }
}
