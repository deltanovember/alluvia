package com.alluvia.tools.web

import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import com.google.common.io._

class Scraper {
  def fetchPage(url: String): String = {
    var client: HttpClient = new DefaultHttpClient
    var get: HttpGet = new HttpGet(url)
    try {
      var response: HttpResponse = client.execute(get)
      var data: InputStream = response.getEntity.getContent

      try {
        var content = CharStreams.toString(new InputStreamReader(data))
        return content
      }
      finally {
        Closeables.closeQuietly(data)
      }
    }
    catch {
      case e: IOException => {
        e.printStackTrace
        ""
      }
    }
  }
}


