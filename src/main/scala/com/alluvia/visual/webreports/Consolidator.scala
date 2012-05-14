package com.alluvia.visual.webreports

import io.Source
import collection.mutable.HashMap
import com.alluvia.types.MagicMap
import com.alluvia.algo.Toolkit
import java.io.File

/**
 * Consolidates partial fills for reporting e.g.
 *
 * 2011-12-07,55=N/A,0.4,37500.0
2011-12-07,55=N/A,0.495,1746.0
2011-12-07,55=N/A,0.495,7339.0
2011-12-07,55=N/A,0.495,3896.0
2011-12-07,55=N/A,0.495,2936.0
2011-12-07,55=N/A,0.495,2936.0
2011-12-07,55=N/A,0.495,8012.0
2011-12-07,55=N/A,0.495,3438.0
2011-12-07,55=N/A,0.16,90909.0
2011-12-07,55=N/A,0.53,1482.0
2011-12-07,55=N/A,0.53,4906.0
 */

object Consolidator extends Toolkit {

  def main(args: Array[String]) {
    val solo = "target\\scala-2.9.1.final\\classes\\Solo.temp"
    val sunset = "target\\scala-2.9.1.final\\classes\\sunset.temp"
    consolidate(solo)
    consolidate(sunset)

    def consolidate(file: String) {
      val trades = MagicMap[String](0.0)
      val source = Source.fromFile(file)
      if (new File(file).exists()) {

        for (line <- source.getLines()) {
          val tokens = line.split(",")
         // println(line)
          if (tokens.length > 3) {

            //val tradeInfo = (tokens.take(tokens.length).take(3).toList ::: tokens.takeRight(4).toList).mkString(",")
            val volume = tokens(3).toDouble
            tokens(3) = "0"
            val tradeInfo = tokens.mkString(",")
            trades.put(tradeInfo, volume + trades(tradeInfo))
          }

        }
        source.close
        val history = file.replace("temp", "history").split("\\\\").takeRight(1)(0)
        trades.keys.foreach {
          x => val volume = trades(x)
          val tokens = x.split(",")
          tokens(3) = volume.toString
          println(tokens.mkString(","))
          appendcsv(history, tokens.mkString(","))
        }
      }

      new File(file).delete()
    }
  }


}