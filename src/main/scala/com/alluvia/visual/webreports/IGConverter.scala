package com.alluvia.reports

import java.io.File
import io.Source
import java.math.RoundingMode
import scala.math.abs
import scala.Predef._
import collection.mutable.{HashMap, ListBuffer}
import java.text.{SimpleDateFormat, DecimalFormat}
import com.alluvia.markets.Market
import com.alluvia.algo.Toolkit
import com.alluvia.visual.webreports.IGBase

abstract class IGConverter extends IGBase with Market with Toolkit {

  type DataLine = (String, String, Int, Double, Double, Double, Double, String)
  val dir = "data"
  val filtered = new File(dir).list().filter(name => name.contains(".ig"))

  //filtered.fore

  convert("Solo.ig")
  convert("Sunset.ig")

  def convert(strategyFile: String) {
    val strategyName = strategyFile.split("\\.")(0)
    val profitHistory = new ListBuffer[DataLine]
    val consolidated = new ListBuffer[DataLine]
    val eventsXMLFileName = strategyName.toLowerCase + "events" + getMarketName + ".xml"


    var runningAmount = 0.0
    var profit = 0.0
    var grossProfit = 0.0
    var commissionTotal = 0.0
    var interestTotal = 0.0
    var trades = 0
    var turnover = 0.0

    // Separate by strategy
    val sorted = Source.fromFile(dir + "\\" + strategyFile).getLines.toList.reverse
    for (line <- sorted) {

      val lineTokens = line.split(",")
      val summary = lineTokens(1)
      val date = lineTokens(2)
      val reference = lineTokens(3)
      val description = lineTokens(4)
      val opening = lineTokens(6)
      val currency = lineTokens(7)
      val size = lineTokens(8)
      val price = lineTokens(9)
      val amount = lineTokens(10)

      if (currency.contains(getIGCurrencySymbol)) {
        // roll commissions and interest into one transaction
        runningAmount += amount.toDouble
        val commissions = description.contains("COMM")
        val interest = description.contains("nterest")
        val dividend = description.contains("ividend")

        if (!commissions &&
          !interest &&
          !dividend) {

          profit += runningAmount
          val valueTraded = abs(size.toInt) * opening.toDouble / getBrokerCurrencyMultiplier +
            abs(size.toInt) * price.toDouble / getBrokerCurrencyMultiplier
          val dataLine = (fixDate(date), "00:00:01".toString,
            (size.toInt), round(profit), round(runningAmount), round(price.toDouble), valueTraded, description)
          profitHistory.append(dataLine)
          addConsolidated(dataLine, consolidated)
          runningAmount = 0
        }

        if (commissions) {
          commissionTotal += amount.toDouble
        }

        if (interest) {
          interestTotal += amount.toDouble
        }

        if (summary == "Closing trades") {
          trades += 2;
          grossProfit += amount.toDouble
          println("*", line, grossProfit)
        }

        if (price.toDouble > 0) {
          turnover += abs(size.toInt) * (opening.toDouble + price.toDouble) / getBrokerCurrencyMultiplier
        }
      }

    }

    profitHistory.foreach(println)

    // Flag data
    val xmlFile = new ListBuffer[String]
    val dataFile = new ListBuffer[String]
    val templateDest = new ListBuffer[String]


    // Generate data file
    consolidated.reverse.foreach(line => dataFile.append(line._1 + "," + round(line._7) + "," + line._4))

    val buys = profitHistory.filter(_._3 > 0)
    val sells = profitHistory.filter(_._3 < 0)
    val dateCount = new HashMap[String, Int]

    val eventsFile = getEventsFile(buys, sells, dateCount)

    // Load settings
    val settingsSource = Source.fromURL(getClass.getResource("/amstock_settings.xml"))
    val templateSource = Source.fromURL(getClass.getResource("/amstock_template.html"))

    // Embed live data
    for (line <- settingsSource.getLines()) {
      if (line.contains("<data>")) {
        xmlFile.append(line)
        dataFile.foreach(xmlFile.append(_))
      }
      else if (line.contains("events_file_nam")) {
        xmlFile.append("<events_file_name>" + eventsXMLFileName + "</events_file_name>")
      }
      else if (line.contains("Header")) {
        xmlFile.append("<text>" + strategyName + " " + getMarketName + ", Trades: " + trades + ", Turnover: " + round(turnover) + ", Commissions: " + round(commissionTotal) + ", Gross trading profit: " + round(grossProfit)  + ", Interest: " + round(interestTotal) + ", Return on Risk (bps): " + round((profit / turnover * 10000)) + "</text>")
      }
      else {
        xmlFile.append(line)
      }
    }

    // Tweak template
    for (line <- templateSource.getLines()) {
      if (line.contains("XMLFILE")) {
        templateDest.append("so.addVariable(\"settings_file\", encodeURIComponent(\"" + strategyName.toLowerCase + getMarketName + ".xml\"));")
      }
      else {
        templateDest.append(line)
      }
    }

    // Trade data
    printToFile(new File(dir + "\\amstock" + getMarketName + ".xml"))(p => xmlFile.foreach(p.println))
    printToFile(new File(dir + "\\events" + getMarketName + ".xml"))(p => eventsFile.foreach(p.println))
    printToFile(new File(dir + "\\amstock" + getMarketName + ".html"))(p => templateDest.foreach(p.println))

    // Main IFrame file
    pscpToServer(dir, "reporting.html", "profit", "index.html", 60)

    pscpToServer(dir, "amstock" + getMarketName + ".html", "profit", strategyName.toLowerCase() + getMarketName + ".html", 60)
    pscpToServer(dir, "events" + getMarketName + ".xml", "profit", eventsXMLFileName, 60)

    // Trade data
    pscpToServer(dir, "amstock" + getMarketName + ".xml", "profit", strategyName.toLowerCase + getMarketName + ".xml", 60)

  }


  def addConsolidated(line: DataLine, consolidated: ListBuffer[DataLine]) {
    if (consolidated.size == 0) {
      consolidated.append(line)
    }
    //new date
    else if (consolidated.last._1 != line._1) {
      consolidated.append(line)
    }
    else {
      val last = consolidated.last
      val newProfit = line._4
      val newData = last.copy(_4 = newProfit, _5 = newProfit, _6 = newProfit, _7 = round(line._7 + last._7))
      consolidated.trimEnd(1)
      consolidated.append(newData)
    }

  }

  def getEventsFile(buys: ListBuffer[DataLine], sells: ListBuffer[DataLine], dateCount: HashMap[String, Int]): ListBuffer[String] = {

    val bulletSize = 7
    val eventsFile = new ListBuffer[String]
    eventsFile.append("<events>")
    for (line <- buys) {

      val date = line._1
      if (!dateCount.contains(date)) dateCount.put(date, 0)
      else dateCount.put(date, dateCount(date) + 3)
      val split = line._1.split("-")
      val day = if (split(0).charAt(0) == '0') split(0).charAt(1) else split(0)

      eventsFile.append("  <event>")
      eventsFile.append("    <color>#66FF99</color>")
      eventsFile.append("    <size>" + bulletSize + "</size>")
      eventsFile.append("    <date>" + date + "</date>")
      //eventsFile.append("    <letter>B</letter>")
      eventsFile.append("    <description><![CDATA[" + line._8 + ": " + abs(line._3) + " @ " + line._6 + ", Profit: " + line._5 + "]]></description>")
      eventsFile.append("  </event>")
    }


    for (line <- sells) {
      val date = line._1
      if (!dateCount.contains(date)) dateCount.put(date, 0)
      else dateCount.put(date, dateCount(date) + 3)
      val split = line._1.split("-")
      val day = if (split(0).charAt(0) == '0') split(0).charAt(1) else split(0)
      eventsFile.append("  <event>")
      eventsFile.append("    <color>#FF6666</color>")
      eventsFile.append("    <date>" + date + "</date>")
      eventsFile.append("    <size>" + bulletSize + "</size>")
      //eventsFile.append("    <letter>S</letter>")
      eventsFile.append("    <description><![CDATA[" + line._8 + ": " + abs(line._3) + " @ " + line._6 + ", Profit: " + line._5 + "]]></description>")
      eventsFile.append("  </event>")
    }
    eventsFile.append("</events>")
    eventsFile
  }

  def fixDate(date: String) = {

    val parser = new SimpleDateFormat("dd/MM/yy")
    val dateObject = parser.parse(date);

    val isoParser = new SimpleDateFormat("yyyy-MM-dd");
    isoParser.format(dateObject);


  }

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
        false
      }
      catch {
        case e: IllegalThreadStateException => {
          true
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
        ex.printStackTrace()
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
    if (unrounded.isNaN)
      unrounded
      else
      df.format(unrounded).toDouble
  }

}