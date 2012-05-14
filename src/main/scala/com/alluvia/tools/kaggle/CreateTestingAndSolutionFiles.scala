package com.alluvia.tools.kaggle

import collection.mutable.ListBuffer
import io.Source
import com.alluvia.algo.Toolkit

/**
 * Separate raw CSV into predictor/response
 */

object CreateTestingAndSolutionFiles extends App with Toolkit {

  val files = List("testing_in.csv")
  val csv = new ListBuffer[List[String]]
  files.foreach {
    file =>
      for (line <- Source.fromFile(file).getLines) {
        csv.append(line.split(",").toList)
      }
      val (predictor, response) = split(csv, "bid51")
      val (rowid, remainder) =  split(predictor, "security_id")
      val responseWithId = rowid.zip(response)
      predictor.foreach(row => printcsv("testing.csv", row.mkString(",")))
      responseWithId.foreach(row => printcsv("solution.csv", row._1.head, row._2.mkString(",")))
      csv.clear()
  }

  def split(doc: Seq[Seq[String]], search: String) = {
    val i = doc.head.indexOf(search)
    doc.map(_.splitAt(i)).unzip
  }

}