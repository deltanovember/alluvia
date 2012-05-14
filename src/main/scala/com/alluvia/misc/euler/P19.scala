package com.alluvia.misc.euler

import com.alluvia.algo.TypeConverter


object P19 extends App with TypeConverter {

  var date: java.util.Date = "1901-01-01"
  var count = 0
  while (date.toIso != "2001-01-01") {
    val dateString = date.toIso
    if (date.day == 1 && dateString.substring(dateString.length()-2,dateString.length())== "01") count += 1
    date = (date + 1.day)
  }
  println(count)
}