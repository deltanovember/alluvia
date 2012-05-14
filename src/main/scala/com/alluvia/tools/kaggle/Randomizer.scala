package com.alluvia.tools.kaggle

/**
 * Randomize liquidity shocks
 */

import io.Source
import java.io.FileWriter
import com.alluvia.algo.Toolkit
import collection.mutable.{HashSet, ListBuffer}

object Randomizer extends App with Toolkit {
  var row = 0
  var rowStart = 704018
  var header = ""
  val data = new ListBuffer[String]
  val dups = new HashSet[String]

  for (line <- Source.fromFile("testing_in_all.csv").getLines) {
    if (0 == row) printcsv("testing_in.csv", "row_id," + line)
    else if (!dups.contains(line)) data += line
    row += 1
    dups.add(line)
  }
  val randomized = data.toArray
  java.util.Collections.shuffle(java.util.Arrays.asList(randomized: _*))
  for (i <- 1 to 50000) printcsv("testing_in.csv", (rowStart + i) + "," + randomized(i))

}