package com.alluvia.visual.spread2


import java.io.File
import io.Source
import java.sql._
import collection.mutable.ListBuffer
import java.math.RoundingMode
import java.text.{SimpleDateFormat, DecimalFormat}


class Spread2 {

  // ===================================================================================================================
  // Generate Chart Data
  // ===================================================================================================================



  // ===================================================================================================================
  // Upload to Server
  // ===================================================================================================================

  // Flag data
  val htmlFile = new ListBuffer[String]
  val dataFile = new ListBuffer[String]
  val eventsFile = new ListBuffer[String]

  val source = Source.fromFile("C:/Users/mclifton.CM-CRC/Alluvial/Code/alluvia/target/scala-2.9.1.final/classes/index.htm")
  val dataSource = Source.fromFile("C:/TEMP/alldataASX201101.csv")
//  val source = Source.fromURL(getClass.getResource("/index.htm"))
//  val dataSource = Source.fromURL(getClass.getResource("/data.csv"))
//  println(System.currentTimeMillis())

  for (line <- source.getLines) {
    htmlFile.append(line)
  }

  for (line <- dataSource.getLines) {
    dataFile.append(line)
  }

  printToFile(new File("index.html"))(p => {
    htmlFile.foreach(p.println)
  })

  pscpToServer(".", "index.html", "profit/spread", "index.html", 60)
  pscpToServer(".", "trades.csv", "profit/spread", "trades.csv", 60)
  pscpToServer(".", "asks.csv", "profit/spread", "asks.csv", 60)
  pscpToServer(".", "bids.csv", "profit/spread", "bids.csv", 60)
  pscpToServer(".", "spreads.csv", "profit/spread", "spreads.csv", 60)

  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try {
      op(p)
    } finally {
      p.close()
    }
  }

  /**
   * use pscp to transfer files to the server
   * @localDir - Directory including the trailing slash
   * @remoteDir - Directory without the trailing slash
   *
   */
  def pscpToServer(localDir: String, localFile: String, remoteDir: String, remoteFile: String, timeoutInSeconds: Long): Unit = {
    def isAlive(p: Process): Boolean = {
      try {
        p.exitValue
        return false
      }
      catch {
        case e: IllegalThreadStateException => {
          return true
        }
      }
    }
    val cleanedDir = if (localDir.charAt(localDir.length - 1) != '\\') localDir + "\\" else localDir

    var command: String = "pscp -q -P 22 -pw 67a5ec9705eb7ac2c98 " + cleanedDir + localFile + " dnguyen@alluvial-web:\"/var/www/html/" + remoteDir + "/" + remoteFile + "\""
    System.out.println("running - " + command)
    try {
      var process: Process = Runtime.getRuntime.exec(command)
      if (timeoutInSeconds <= 0) {
        process.waitFor
      }
      else {
        var now: Long = System.currentTimeMillis
        var timeoutInMillis: Long = 1000L * timeoutInSeconds
        var finish: Long = now + timeoutInMillis
        while (isAlive(process) && (System.currentTimeMillis < finish)) {

          Thread.sleep(1000)
        }
        if (isAlive(process)) {
          throw new InterruptedException("Process timeout out after " + timeoutInSeconds + " seconds")
        }
      }
      var returnCode: Int = process.waitFor
      if (0 == returnCode) {
        System.out.println("finished normally")
      }
      else {
        System.err.println("Abnormal pscp termination")
      }
    }
    catch {
      case ex: Exception => {
        ex.printStackTrace
      }
    }
  }

  def storeHistorical(args: Any*) {
    val line = args.mkString(";")
    //profitHistory.append(line)
  }

  def round(unrounded: Double): Double = {
    var df: DecimalFormat = new DecimalFormat("#.##")
    df.setRoundingMode(RoundingMode.HALF_UP)
    df.format(unrounded).toDouble
  }

}



