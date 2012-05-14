package com.alluvia.tools.web

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import com.google.common.io.ByteStreams
import com.google.common.io.Closeables

object FileGrabber {
  def main(args: Array[String]): Unit = {
    var client: HttpClient = new DefaultHttpClient
    var get: HttpGet = new HttpGet("http://www.marketdatasystems.com/content/files/Tiered%20Margin.xls")
    try {
      var response: HttpResponse = client.execute(get)
      var data: InputStream = response.getEntity.getContent
      try {
        var output: OutputStream = new FileOutputStream(new File("c:\\temp\\margin.xls"))
        ByteStreams.copy(data, output)
      }
      finally {
        Closeables.closeQuietly(data)
      }
    }
    catch {
      case e: IOException => {
        e.printStackTrace
      }
    }
  }
}


