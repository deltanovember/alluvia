package com.alluvia.visual.webreports

import java.io.File
import collection.mutable.ListBuffer
import io.Source

class IGBase {

  def getAllTransactions() = {

    val files = new File(".").list().sortBy(name => new java.io.File(name).lastModified()).reverse
    val filtered = files.filter(name => name.contains("txn"))

    val masterList = new ListBuffer[String]

    for (file <- filtered) {
      val lines = Source.fromFile(new File(file), "UTF-16").getLines.drop(1).filter(
        x => x.contains("CFD") || x.contains("Comm") || x.contains("Closing") || x.contains("Dividen"))
      val cleaned = lines.filter(!masterList.contains(_)).toList
      cleaned.foreach(x => masterList.append(x))
    }
    masterList.map(x => x.replaceAll("\t", ",")).toList

  }

  def getProfitLine(id: Int, line: String) = {
    val lineTokens = line.split(",")
    val transType = lineTokens(0)
    val summary = lineTokens(1)
    val date = lineTokens(2)
    val reference = lineTokens(3)
    val description = lineTokens(4)
    val period = lineTokens(5)
    val opening = lineTokens(6)
    val currency = lineTokens(7)
    val size = lineTokens(8)
    val price = lineTokens(9)
    val amount = lineTokens(10).toDouble
    ProfitLine(id, transType, summary, date, reference, description, period, opening, currency, size, price, amount)
  }
}