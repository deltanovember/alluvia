package com.alluvia.tools.reuters

/**
 * Extracts a particular security
 */

import scala.io.Source
import java.io.File
import java.nio.charset.MalformedInputException

object ReutersImporter {

  var printLine = false
  val dir = "."

  def main(args: Array[String]) {

    if (args.length < 2) {
      println("Usage: ReutersImporter SEARCHSTRING SECURITY")
    }
    val files = new File(dir).list().filter(_.contains(args(0))).sortBy(_.toString)
    for (file <- files) {
      try {
        for (line <- Source.fromFile(dir + System.getProperty("file.separator") + file).getLines) {
          val allTokens = line.split(",", -1)
          val validTokens = line.split(",")
          // RIC
          if ("" != allTokens(0)) {
            if (args(1) == allTokens(0)) {
              println(line)
              printLine = true
            }
            else {
              printLine = false
            }

          }
          else if (printLine) {
            println(line)
          }

        }
      }
      catch {
        case ex: MalformedInputException =>

      }
    }

  }

}