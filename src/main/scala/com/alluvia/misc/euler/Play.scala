package com.alluvia.misc.euler

import java.io.{File, FileWriter}

object Play extends App {

  def appendcsv(fileName: String, args: Any*) {
    val fw = new FileWriter(fileName, true)
    val line = args.mkString(",")
    fw.write(line + "\r\n")
    fw.close()
  }
  def printcsv(fileName: String, args: Any*) {
    appendcsv(fileName, args)
  }
  appendcsv("test.csv", "string1", "string2")
  printcsv("test.csv", "string1", "string2")
  

}