package com.alluvia.tools

import java.io.File
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.util.PDFTextStripper
import java.lang.StringBuffer


class PDFReader {
  def readPDF(file: File): Array[String] = {
    val text = new StringBuffer()
    try {
      var doc: PDDocument = PDDocument.load(file)
      var stripper: PDFTextStripper = new PDFTextStripper
      stripper.setEndPage(doc.getNumberOfPages)
      var firstPage: String = stripper.getText(doc)
      var tokens: Array[String] = firstPage.split(System.getProperty("line.separator"))
      doc.close
      tokens
    }
    catch {
      case ex: Exception => {
        ex.printStackTrace
        Array()
      }
    }

  }
}

