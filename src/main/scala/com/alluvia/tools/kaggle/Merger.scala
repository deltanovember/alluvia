package com.alluvia.tools.kaggle

/**
 * Append testing.csv to training.csv
 */

import io.Source
import com.alluvia.algo.Toolkit
import collection.mutable.{HashSet, ListBuffer}

object Merger extends App with Toolkit {

  var row = 0

//  var rowStart = 704018
//  for (line <- Source.fromFile("testing.csv").getLines().drop(1)) {
//    row += 1
//    appendcsv("training.csv", (rowStart + row) + "," + line.split(",").toList.tail.mkString(","))
//  }

  for (line <- Source.fromFile("Liquidity Replenishment 201107.csv").getLines()) {
    printcsv("testing_in_all.csv", line)
  }
  for (line <- Source.fromFile("Liquidity Replenishment 201108.csv").getLines().drop(1)) {
    printcsv("testing_in_all.csv", line)
  }
  for (line <- Source.fromFile("Liquidity Replenishment 201109.csv").getLines().drop(1)) {
    printcsv("testing_in_all.csv", line)
  }

}