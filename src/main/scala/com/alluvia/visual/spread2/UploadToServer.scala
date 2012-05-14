package com.alluvia.visual.spread2

/**
 * Upload spread files to server
 */

import io.Source
import java.io.File
import com.alluvia.algo.Toolkit
import collection.mutable.{HashMap, ListBuffer}

object UploadToServer extends App with Toolkit {

  val runDate = "20111128"
  val dir = "C:/Users/mclifton.CM-CRC/Alluvial/Code/alluvia/src/main/scala/com/alluvia/visual/spread2/spreaddata/"
  val serverDir = "profit/spread/"
  val fileNameHtml = "index.html"
  val fileNameSpread = "spread/spread_"  // data/yyyymmdd/spread/spread_SEC.csv
  val fileNameConfigSecurity = "config/config_security.csv"  // data/yyyymmdd/config/config_security.csv
  val fileNameConfigMarket = "config_market.csv" // data/config_market.cs

  val securityList = new HashMap[String, Boolean]


  // Get security list
  for (line <- Source.fromFile(dir + "/data/" + runDate + "/" + fileNameConfigSecurity).getLines()) {
    securityList.put(line.split(",")(0), true)
  }


  println("Uploading to Server");

//  // Index file
//  println("Uploading index file")
//  val htmlFile = new ListBuffer[String]
//  val source = Source.fromFile(dir + fileNameHtml)
//  for (line <- source.getLines()) {
//    htmlFile.append(line)
//  }
//  printToFile(new File(dir + fileNameHtml))(p => {
//    htmlFile.foreach(p.println)
//  })
//  pscpToServer(dir, fileNameHtml, serverDir, fileNameHtml, 60)
//
//  // Security config files
//  println("Uploading security config file")
//  pscpToServer(dir, "data/" + runDate + "/" + fileNameConfigSecurity, serverDir, "data/" + runDate + "/" + fileNameConfigSecurity, 60)


  pscpDirToServer()

//  // Security files
//  println("Uploading security files")
//  securityList.keys.foreach(
//    security => {
//      val fileNameSpread_ = "data/" + runDate + "/" + fileNameSpread + security + ".csv"
//      pscpToServer(dir, fileNameSpread_, serverDir, fileNameSpread_, 60)
//    }
//  )

  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try {
      op(p)
    } finally {
      p.close()
    }
  }

  def pscpDirToServer(): Unit = {
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

//    var command: String = "pscp -q -P 22 -pw 67a5ec9705eb7ac2c98 " + dir + localFile + " dnguyen@alluvial-web:\"/var/www/html/" + remoteDir + remoteFile + "\""
    var command: String = "pscp -q -R -P 22 -pw 67a5ec9705eb7ac2c98 " + dir + " * dnguyen@alluvial-web:\"/var/www/html/" + serverDir + "\""
    System.out.println("running - " + command)
    val timeoutInSeconds = 60
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

  // Use pscp to transfer files to the server
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

//    var command: String = "pscp -q -P 22 -pw 67a5ec9705eb7ac2c98 " + dir + localFile + " dnguyen@alluvial-web:\"/var/www/html/" + remoteDir + remoteFile + "\""
    var command: String = "pscp -q -R -P 22 -pw 67a5ec9705eb7ac2c98 " + dir + localFile + " dnguyen@alluvial-web:\"/var/www/html/" + remoteDir + remoteFile + "\""
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

}