package com.alluvia.tools.reuters

import java.io.File
import io.Source
import java.nio.charset.MalformedInputException

/**
 * Check that quotes always follow trades. Uses data from
 * ReutersImporter
 */

object QuoteChecker {
  var printLine = false
  var lastTransType = ""
  var lastLine = ""
  var lastTimeStamp = ""

  def main(args: Array[String]) {

    val files = new File(".").list().filter(_.contains(args(0))).sortBy(_.toString)
    for (file <- files) {
      try {
        for (line <- Source.fromFile(file).getLines) {
          val allTokens = line.split(",", -1)

          if (allTokens.length > 5) {
            val transType = allTokens(7)


            if (transType == "TRDTIM_MS" ||
              transType == "OFFBK_PRC" ||
              transType == "OFFBK_VOL" ||
              transType == "QUOTIM_MS" ||
            transType == "PDTRDPRC") {
            if (lastTransType == "TRDTIM_MS" &&
              transType != "QUOTIM_MS" &&
              transType != "OFFBK_VOL" &&
            transType != "PDTRDPRC") {
              println("fail" + lastLine)
 println("fail" + line)
              System.exit(0)
            }
              if (transType == "TRDTIM_MS") {
                lastTimeStamp = allTokens(8)
                println("trade")
              }
              if (transType == "QUOTIM_MS") {
                lastTimeStamp = allTokens(8)
              }

              lastTransType = transType
              lastLine = line
              // println(transType)
            }

          }


        }
      }
      catch {
        case ex: MalformedInputException =>

      }
    }

  }

  println("passed")
}