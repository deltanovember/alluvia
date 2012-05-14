package com.alluvia.types

case class Date(date: String) {

  // regex
  val dateRegex = """([0-9]{4}-[0-9]{2}-[0-9]{2})"""
  val timeRegex = """([0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3})"""
    
  val DateTime = (dateRegex + " " + timeRegex).r
  val DateOnly = dateRegex.r
  
  def get(): String = date match {
    case DateTime(d, t) => d + " " + t
    case DateOnly(d) => d + " 00:00:00.000"
  }
}