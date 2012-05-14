package com.alluvia.zzz

import io.Source
import com.alluvia.algo.Toolkit
import collection.mutable.{HashSet, HashMap, ListBuffer}


object Extract extends Toolkit {
  def main(args: Array[String]) {

    val hash = new HashSet[String]

    for (line <- Source.fromFile("c:\\temp\\southcoast\\QMF_MASTER.TXT").getLines) {
      val tokens = line.replaceAll("\"","").split(",")
      if (tokens.length != 13 && tokens.length != 14 && tokens.length != 15) println(tokens.length)
      val newTokens: Array[String] = if (tokens.length == 13) {
        tokens ++ Array("", "", "")
      }
      else if (tokens.length == 14) {
        tokens  ++ Array("", "")
      }
      else {
        tokens  ++ Array("")
      }

      val newString = newTokens.mkString(",")
      hash.add(newString)
    }

    hash.foreach(x => printcsv("c:\\temp\\southcoast\\clean.txt", x))

  }
}