package com.alluvia.algo

import collection.mutable.HashSet
import java.io.{FileNotFoundException, File, FileWriter}
import java.text.{DecimalFormat, ParsePosition, NumberFormat}
import java.math.RoundingMode

/**
 * Generic library functions
 */

trait Toolkit {

  val fileStore = new HashSet[String]

  def appendcsv(fileName: String, args: Any*) {
    val fw = new FileWriter(fileName, true)
    val line = args.mkString(",")
    fw.write(line + "\r\n")
    fw.close()
  }

  def isNumeric(input: String): Boolean = {
    val formatter = NumberFormat.getInstance
    val pos = new ParsePosition(0)
    formatter.parse(input, pos)
    input.length == pos.getIndex // valid if parse position is at end of string
  }

  def printcsv(fileName: String, args: Any*) {
    if (!fileStore.contains(fileName)) {
      fileStore.add(fileName)
      val file = new File(fileName)
      if (file.exists()) file.delete()
    }
    if (fileName.contains("\\")) {
      val dir = fileName.split("\\\\")(0)
      if (!new File(dir).exists()) new File(dir).mkdir()
    }

    try {
      val fw = new FileWriter(fileName, true)
      val line = args.mkString(",")
      fw.write(line + "\r\n")
      fw.close()
    }
    catch {
      case fnfe: FileNotFoundException => fnfe.printStackTrace()
      case e: Exception => e.printStackTrace()
    }

  }


  def round(num: Double, decimals: Int): Double = {
    def getFormat(numDecimals: Int): String = {
      numDecimals match {
        case 0 => "#"
        case 1 => "#.#"
        case _ => getFormat(numDecimals - 1) + "#"
      }
    }

    val df = new DecimalFormat(getFormat(decimals))
    df.setRoundingMode(RoundingMode.HALF_UP)
    df.format(num).toDouble
  }

}