package com.alluvia.algo

import java.util.{Calendar, Date}
import java.text.SimpleDateFormat
import com.alluvialtrading.tools.TraderLib

trait TypeConverter {

  val lib = new TraderLib

  // Date parsing
  val dateRegex = """([0-9]{4}-[0-9]{2}-[0-9]{2})"""
  val timeRegex = """([0-9]{2}:[0-9]{2}:[0-9]{2})"""
  val timeRegexMillis = """(.[0-9]{3})"""

  val DateTimeMillis = (dateRegex + " " + timeRegex + timeRegexMillis).r
  val DateTime = (dateRegex + " " + timeRegex).r
  val DateOnly = dateRegex.r

 // implicit def dateToDM(d: Date) = new DateMath(d)
    implicit def dateToMyDate(myDate: java.util.Date) = new MyDate(myDate)
  implicit def intToTimeSpanBuilder(in: Int): TimeSpanBuilder =
    TimeSpanBuilder(in)
  implicit def longToTimeSpanBuilder(in: Long): TimeSpanBuilder =
    TimeSpanBuilder(in)

  implicit def stringToDate(dateString: String): java.util.Date = dateString match {
    case DateTimeMillis(d, t, m) => new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(dateString)
    case DateTime(d, t) => new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString)
    case DateOnly(d) => new java.text.SimpleDateFormat("yyyy-MM-dd").parse(dateString)
  }


//  class DateMath(d: Date) {
//
//    def +(ts: TimeSpan) = new Date(d.getTime + ts.millis)
//    def -(ts: TimeSpan) = new Date(d.getTime - ts.millis)
//
//  }


  class MyDate(myDate: java.util.Date) {

    def >(in: java.util.Date) = {
      myDate.after(in)
    }

    def <(in: java.util.Date) = {
      myDate.before(in)
    }

    def +(in: TimeSpan): Date = {
      new Date(myDate.getTime + in.millis)
    }

    def -(in: java.util.Date):TimeSpan = {
      new TimeSpan(myDate.getTime - in.getTime)
    }

    def -(in: TimeSpan): Date = {
      new Date(myDate.getTime - in.millis)
    }
    def day: Int = {
      val calendar = java.util.Calendar.getInstance()
      calendar.setTime(myDate); // assigns calendar to given date
      calendar.get(Calendar.DAY_OF_WEEK); // gets hour in 24h format
    }
    def endOfDay: Date = {
      lib.combineDateTime(myDate, "23:59:59.999")
    }
    def hours: Int = {
      val calendar = java.util.Calendar.getInstance()
      calendar.setTime(myDate); // assigns calendar to given date
      calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
    }
    def minutes: Int = {
      val calendar = java.util.Calendar.getInstance()
      calendar.setTime(myDate); // assigns calendar to given date
      calendar.get(Calendar.MINUTE); // gets hour in 24h format
    }
    def seconds: Int = {
      val calendar = java.util.Calendar.getInstance()
      calendar.setTime(myDate); // assigns calendar to given date
      calendar.get(Calendar.SECOND); // gets hour in 24h format
    }
    def startOfDay: Date = {
      lib.combineDateTime(myDate, "00:00:00.001")
    }

    def toCustom(format: String): String = {
       val parser: SimpleDateFormat = new SimpleDateFormat(format)

      try {
        parser.format(myDate)
      }
      catch {
        case ex: Exception => {
          ex.printStackTrace()
          null
        }
      }
    }
    def toDateStr: String = {
      val parser: SimpleDateFormat = new SimpleDateFormat("yyyyMMdd")

      try {
        parser.format(myDate)
      }
      catch {
        case ex: Exception => {
          ex.printStackTrace
          null
        }
      }
    }

     def toDateTime: String = {
       val parser: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

      try {
        parser.format(myDate)
      }
      catch {
        case ex: Exception => {
          ex.printStackTrace()
          null
        }
      }
    }


   def toIso = lib dateToISODateString myDate

   def toTimeStr: String = {
     val parser: SimpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSS")

      try {
        parser.format(myDate)
      }
      catch {
        case ex: Exception => {
          ex.printStackTrace()
          null
        }
      }
    }

  }

  case class TimeSpan(millis: Long) extends Ordered[TimeSpan] {

    def later = new Date(millis + TimeHelpers.millis)
    def ago = new Date(TimeHelpers.millis - millis)
    def +(in: TimeSpan) = TimeSpan(this.millis + in.millis)
    def -(in: TimeSpan) = TimeSpan(this.millis - in.millis)
    def compare(other: TimeSpan) = millis compare other.millis

  }


  case class TimeSpanBuilder(val len: Long) {

    def seconds = TimeSpan(TimeHelpers.seconds(len))
    def second = seconds
    def minutes = TimeSpan(TimeHelpers.minutes(len))
    def minute = minutes
    def hours = TimeSpan(TimeHelpers.hours(len))
    def hour = hours
    def days = TimeSpan(TimeHelpers.days(len))
    def day = days
    def weeks = TimeSpan(TimeHelpers.weeks(len))
    def week = weeks
  }

  object TimeSpan {
    implicit def tsToMillis(in: TimeSpan): Long = in.millis
  }


}