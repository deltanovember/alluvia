package com.alluvia.algo

import com.alluvia.markets.Market
import com.alluvia.database.BackTestingLib
import java.io.FileWriter
import java.text.SimpleDateFormat
import com.alluvialtrading.tools.TraderLib
import java.util.{Calendar, Date}

trait Algo extends Market with TypeConverter with Toolkit {
  //val lib = new TraderLib
  val connector = new BackTestingLib


  def printList(args: Any*) {
    val line = args.mkString(" ")
    println(line)
  }

  def run() {}

  implicit def stringToString(myString: String) = new MyString(myString)



  class MyString(myString: String) {

    def close: String = {
      myString + " " + getCloseTime
    }

    def endOfDay: String = {
      myString + " 00:00:00"
    }

    def endAuctionClose: String = {
      myString + " " + getEndAuctionClose
    }

    def startOfDay: String = {
      myString + " 00:00:00.001"
    }
  }

  class HashMapWithDefaults[KeyType, B](hash: collection.mutable.Map[KeyType, B]) {
    def -> = (a: KeyType) => hash.getOrElseUpdate(a, hash(a))
  }

  implicit def hashToDefaults[A, B](h: collection.mutable.Map[A, B]) = {
    new HashMapWithDefaults(h)
  }

}