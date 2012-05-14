package com.alluvia.tools.kaggle

/**
 * Generate simple solutions for testing
 */

import collection.mutable.ListBuffer
import io.Source
import com.alluvia.algo.Toolkit

object CreateExampleEntries extends App with Toolkit {

  val solutionFile = "testing.csv"
  val solutions = new ListBuffer[List[Double]]
  var row = 0
  val startRow = 754019
  // Use last bid/ask as naive solution
  for (line <- Source.fromFile(solutionFile).getLines) {
    row += 1
    if (row == 1) {
      val header = new ListBuffer[String]
      header.append("row_id")
      for (i <- 51 to 100) { header += "bid" + i; header += "ask" + i }
      printcsv("example_entry_naive.csv", header.mkString(","))
    }
    else {
      val rowData = line.split(",").toList
      val lastBid = rowData(rowData.length - 6)
      val lastAsk = rowData(rowData.length - 5)
      val solutionRow = new ListBuffer[Any]
      val row_id = startRow - 2 + row
      solutionRow.append(row_id)
      for (i <- 1 to 50) {
        solutionRow += lastBid.toDouble
        solutionRow += lastAsk.toDouble
      }
      printcsv("example_entry_naive.csv", solutionRow.mkString(","))
    }

  }
  row = 0
  // Linear interpolation
  for (line <- Source.fromFile(solutionFile).getLines) {
    row += 1
    if (row == 1) {
      val header = new ListBuffer[String]
      header.append("row_id")
      for (i <- 51 to 100) { header += "bid" + i; header += "ask" + i }
      printcsv("example_entry_linear.csv", header.mkString(","))
    }
    else {
      val rowData = line.split(",").toList
      val lastBid = rowData(rowData.length - 6).toDouble
      val lastAsk = rowData(rowData.length - 5).toDouble
      val afterBid = rowData(rowData.length - 2).toDouble
      val afterAsk = rowData(rowData.length - 1).toDouble
      val solutionRow = new ListBuffer[Any]
      solutionRow.append(startRow - 2 + row)
      val bidDelta = lastBid - afterBid
      val askDelta = lastAsk - afterAsk
      for (i <- 1 to 50) {

        solutionRow += afterBid + i.toDouble / 50 * bidDelta
        solutionRow += afterAsk + i.toDouble / 50 * askDelta
      }
      printcsv("example_entry_linear.csv", solutionRow.mkString(","))
    }

  }



}