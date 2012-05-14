package com.alluvia.tools.kaggle

import io.Source
import com.alluvia.algo.Toolkit


object CleanTesting extends Toolkit {

  def main(args: Array[String]) {
    var row = 0
    for (line <- Source.fromFile("testing_in.csv").getLines) {
      val chopped = line.split(",").take(206).map(x => if (x.contains(".") && !x.contains(":")) x.toDouble else x).mkString(",")
      if (0 == row) printcsv("testing_cleaned.csv", "row_id," + chopped)
      else printcsv("testing_cleaned.csv", row + "," + chopped)
      row+=1
    }
  }

}