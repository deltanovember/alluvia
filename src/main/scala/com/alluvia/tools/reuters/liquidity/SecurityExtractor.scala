package com.alluvia.tools.reuters.liquidity

import collection.mutable.HashMap
import io.Source
import java.nio.charset.MalformedInputException
import java.io.{FileWriter, File}

object SecurityExtractor {

  val security = "RIO.L"

  def main(args: Array[String]) {

    for (line <- Source.fromFile("liquidity.csv").getLines) {
      if (line.contains(security) || line.contains("date")) {
        printcsv(security + ".csv", line.replaceAll(";", ","))
      }
    }

    }
  def printcsv(fileName: String, args: Any*) {
    val fw = new FileWriter(fileName, true)
    val line = args.mkString(",")
    fw.write(line + "\r\n")
    fw.close()
  }

}