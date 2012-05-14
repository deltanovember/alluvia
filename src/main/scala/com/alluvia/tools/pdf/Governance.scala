package com.alluvia.tools.pdf

import java.io.File
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.util.PDFTextStripper

object Governance {
  def main(args: Array[String]): Unit = {

    val files = new File("pdfs").list()
    for (file <- files) {
      new Governance("pdfs\\" + file)
    }

  }
}

class Governance(file: String) {

  parseFile()

  def parseFile() {

    var (name, abn, director, last, interest, nature, change, held, classOfShare, acquired, disposed, value, after, natureOfChange, filing) =
      ("", "", "", "", "", "", "", "", "", "", "", "", "", "", "")
    try {
      var doc: PDDocument = PDDocument.load(new File(file))
      var stripper: PDFTextStripper = new PDFTextStripper
      stripper.setEndPage(1)
      var firstPage: String = stripper.getText(doc)
      var tokens: Array[String] = firstPage.split("\r\n")
      for (line <- tokens) {

        if (line.replaceAll(" ", "").contains("Nameofentity")) name = line.split("entity")(1).trim
        if (line.contains("ABN ")) abn = line.split("ABN ")(1).trim
        if (line.contains("Name of Director")) director = line.split("rector ")(1).trim
        if (line.contains("Date of last notice ")) last = line.split("Date of last notice ")(1).trim
        if (line.contains("irect or indirect interest ")) interest = line.split("irect or indirect interest ")(1).trim
        if (line.contains("Interest in ")) nature = line.split("Interest in ")(1).trim
        if (line.contains("Date of change ")) change = line.split("Date of change ")(1).trim
        if (line.contains("of securities held prior to change")) held = line.split("of securities held prior to change")(1).trim
        if (line.contains("Class ")) classOfShare = line.split("Class ")(1).trim
        if (line.contains("Number acquired ")) acquired = line.split("Number acquired ")(1).trim.replaceAll(",", "")
        if (line.contains("Number disposed ")) disposed = line.split("Number disposed")(1).trim
        if (line.contains("$")) value = line.trim.replaceAll(",", "")
        if (line.contains("No. of securities held after change")) after = line.split("No. of securities held after change")(1).trim
        if (line.trim.equals("On market trade")) natureOfChange = line.trim

      }
      doc.close
    }
    catch {
      case ex: Exception => {
        ex.printStackTrace
      }
    }
    println(name, abn, director, last, interest, nature, change, held, classOfShare, acquired, disposed, value, after, natureOfChange, filing)
  }
}

