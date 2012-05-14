package com.alluvia.algo

/**
 * Time helpers
 */

object TimeHelpers {

  def seconds(in: Long): Long = in * 1000L
  def minutes(in: Long): Long = seconds(in) * 60L
  def hours(in: Long): Long = minutes(in) * 60L
  def days(in: Long): Long = hours(in) * 24L
  def weeks(in: Long): Long = days(in) * 7L

  def millis = System.currentTimeMillis
  
}