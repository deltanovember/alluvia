package com.alluvia.tools.kaggle

import io.Source
import collection.mutable.{HashSet, HashMap}

object Checker {

  val check = new HashSet[String]
  def main(args: Array[String]) {
    for (line <- Source.fromFile("fixed.csv").getLines) {
      val key = line.split(",").toList.tail.mkString(",")
      if (check.contains(key)) println(key)
      else check.add(key)
    }
  }
}