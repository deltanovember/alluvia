package com.alluvia.tools.kaggle

import com.alluvia.algo.Toolkit
import com.alluvia.algo.TypeConverter
import io.Source
import java.util.Date

/**
 * Remove dirty lines
 */

object DataCleaner extends Toolkit with TypeConverter {

  // Parameters
  val directory = "data"
  val testing = "testing_in_all.csv"

  val file = directory + "\\" + testing

  def main(args: Array[String]) {

    for (line <- Source.fromFile(file).getLines) {
      val tokens = line.split(",").toList
      var accepted = true
      tokens.drop(4).foreach {
        x => if (isNumeric(x) && (x.toDouble < 0.0001 || x.toDouble > 50000)) accepted = false
      }
      if (!accepted) printcsv(directory + "\\rejected.csv", line)
      else printcsv(directory + "\\accepted.csv", line)
    }

  }

}