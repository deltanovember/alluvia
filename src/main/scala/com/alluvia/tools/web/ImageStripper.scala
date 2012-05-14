package com.alluvia.tools.web

import org.apache.http.client.HttpClient
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpGet
import java.io.{FileOutputStream, IOException, File}
import com.google.common.io.{ByteStreams, Closeables}


object ImageStripper {

  def main(args: Array[String]) {

//
//    val client: HttpClient = new DefaultHttpClient
//    val get = new HttpGet("http://themes.truethemes.net/Karma-HTML/images/_pricing_tables/checkmark.png")
//    try {
//      val response = client.execute(get)
//      val data = response.getEntity.getContent
//      try {
//        val output = new FileOutputStream(new File("c:\\temp\\download.png"))
//        ByteStreams.copy(data, output)
//      }
//      finally {
//        Closeables.closeQuietly(data)
//      }
//    }
//    catch {
//      case e: IOException => {
//        e.printStackTrace
//      }
//    }
//    exit(0)
    //

    val base = "http://themes.truethemes.net/Karma-HTML/"
    val page = readURL(base + "style.css")
    val tokens = page.split("\n")
    val images = tokens.filter(x => x.contains("images/")).map(x => base + x.split("\\(")(1).split("\\)")(0).replaceAll ("\"", "").replace("../", ""))
    images.foreach {x =>
      println(x)
      getURLContent(x, "c:\\temp\\images\\" + x.split("images/").takeRight(1)(0).replaceAll ("/", "\\\\"))
    }
  }

  def getURLContent(url: String, destination: String)  {
    println(destination)
    val dir = destination.split("\\\\").dropRight(1).mkString("\\\\")
    if (!new File(dir).exists()) new File(dir).mkdirs()
    val client: HttpClient = new DefaultHttpClient
    val get = new HttpGet(url)
    try {
      val response = client.execute(get)
      val data = response.getEntity.getContent
      try {
        val output = new FileOutputStream(new File(destination))
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
  def readURL(url:String):String = io.Source.fromURL(url).mkString
}