package com.alluvia.tools.kaggle

import io.Source
import com.alluvia.algo.Toolkit
import collection.mutable.{HashSet, HashMap}


object Fixer extends Toolkit {

  val broken = "testing.csv"
  val source = "testing_cleaned.csv"
  val clean = new HashMap[String, String]


  def getKey(line: String) = {
    val tokens = line.split(",").tail
    if (line.contains("value")) {
      tokens(0) + "," + tokens(1) + "," + tokens(3) + "," + tokens(4) + "," + tokens(5) + "," + tokens(6) + "," + tokens(10) + "," + tokens(14) + "," + tokens(18) + "," + tokens(22) + "," + tokens(26) + "," + tokens(30) + "," + tokens(34) + "," + tokens(38) + "," + tokens(200) + "," + tokens(201) + "," + tokens(204) + "," + tokens(205)
  
    }
    else
      tokens.filter(x => !x.contains(":")).mkString(",").replaceAll(".000", "")
  }

  def main(args: Array[String]) {
    for (line <- Source.fromFile(source).getLines) {
      val key = getKey(line)
//      if (clean.contains(key)) {
//        println(key)
//        println("1," + clean(key))
//        println(line)
//        System.exit(0)
//      }
      clean.put(key, line.split(",").toList.mkString(","))

    }
    var count = 0
    val fixedSet = new HashSet[String]

    for (line <- Source.fromFile(broken).getLines) {
      val key = getKey(line)
      //println(key.length())
     // System.exit(0)
      if (clean.contains(key)) {
        val row = line.split(",")(0)
        val fixed = row + "," + clean(key)
        if (0 == count) printcsv("fixed.csv", line)
        else printcsv("fixed.csv", fixed)
        count += 1
        if (fixedSet.contains(key)) {
          println(line)
          println(clean(key))
          System.exit(0)
        }
        else
          fixedSet.add(clean(key))
      }
      else {
        println("key not found", key)
        System.exit(0)
      }
    }
  }
}