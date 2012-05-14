package com.alluvia.tools.reuters

import scala.io.Source
import java.nio.charset.MalformedInputException
import java.io.{FileWriter, File}
import collection.mutable.HashMap

object TasToAlluvia {

  var printLine = false
  val dir = "raw"
  val lastBid = new HashMap[String, String].withDefaultValue("")
  val lastAsk = new HashMap[String, String].withDefaultValue("")

  def main(args: Array[String]) {

    println("Converting Reuters data")
    var ignoreTrade = false
    val files = new File(dir).list().filter(_.contains("TAS")).sortBy(_.toString)
    for (file <- files) {
      try {

        var bidOrAsk = ""
        var date = ""
        var price = ""
        var security = ""
        var volume = ""

        for (line <- Source.fromFile(dir + "\\" + file).getLines) {
          val allTokens = line.split(",", -1)
          val validTokens = line.split(",")

          if (line.contains("ASK,")) {
            bidOrAsk = "S" // consistency with Alluvia format
          }
          if (line.contains("BID,")) {
            bidOrAsk = "B"
          }
          if (line.contains("OFFBK_VOL")) {
            ignoreTrade = true
          }
          if (line.contains("INSTRUMENT_UPDATE_UNSPECIFIED")) {
            date = allTokens(1) + " " + allTokens(2)
            security = allTokens(0)
            if ("" == security) {
              println("debug")
            }
          }
          else if (line.contains("ASK,") || line.contains("BID,")) {
            price = allTokens(8)
          }
          else if (line.contains("ASKSIZE,") || line.contains("BIDSIZE,")) {
            volume = allTokens(8)
          }
          else if (line.contains("TRDPRC_1")) {
            price = allTokens(8)
          }
          else if (line.contains("TRDVOL_1")) {
            volume = allTokens(8)
          }

          // end quote
          else if (line.contains("QUOTIM_MS") && allTokens(8) != "" && price != "") {
            var ignore = false
            if (bidOrAsk == "B") {
              if (lastBid(security) == price) {
                ignore = true
                //println("ignoring bid")
                //  System.exit(0)
              }
              else {
                lastBid.put(security, price)
              }
            }
            else {
              if (lastAsk(security) == price) {
                ignore = true
              }
              else {
                lastAsk.put(security, price)
              }
            }
            if (security == "GLEN.L" && line.contains("38658318")) {
              println("debug")
            }
            if (!ignore)
             printcsv("2011-09-14.csv", "ENTORD", date, security, "E", price, volume, price.toDouble * volume.toDouble, bidOrAsk)
          }
          else if (line.contains("TRADE_ID") && allTokens(8) != "") {

            if (!ignoreTrade) {
              printcsv("2011-09-14.csv", "TRADE", date, security, "E", price, volume, price.toDouble * volume.toDouble, bidOrAsk)
            }

            ignoreTrade = false
          }

        }

      }
      catch {
        case ex: MalformedInputException =>

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