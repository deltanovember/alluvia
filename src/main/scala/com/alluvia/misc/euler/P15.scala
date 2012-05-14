package com.alluvia.misc.euler

import collection.mutable.ListBuffer
import java.math.BigInteger

object P15 {

  def main(args: Array[String]) {
    val gridSize = 20
    def fact(num: BigInteger, acc: BigInteger): BigInteger = {
      if (num.equals(new BigInteger("0"))) acc
      else fact(num.subtract(new BigInteger("1")), num.multiply(acc))
    }
    val forty = new BigInteger((gridSize*2).toString)
    val denom = fact(new BigInteger((gridSize).toString), new BigInteger("1"))
    println(fact(forty, new BigInteger("1")).divide(denom.multiply(denom)))
    //(1 to 40).permutations.size
//    val paths = new ListBuffer[(Byte, Byte)]
//
//    def generatePaths(incomplete: ListBuffer[(Byte, Byte)], gridSize: Byte): ListBuffer[(Byte, Byte)] = {
//      if (incomplete.head._1 == gridSize && incomplete.head._2 == gridSize) {
//        //prByteln(incomplete.head.length)
//        incomplete
//      }
//      else {
//        val newPath = new ListBuffer[(Byte, Byte)]
//        incomplete.foreach{
//          x => if (x._1 + 1 <= gridSize) newPath.append(((x._1 + 1).toByte, x._2.toByte))
//          if (x._2 + 1 <= gridSize) newPath.append((x._1.toByte, (x._2 + 1).toByte))
//        }
//        generatePaths(newPath, gridSize)
//      }
//    }
//
//    paths.append((0, 0))
//    val all = generatePaths(paths, gridSize.toByte)
//    println(all.size)

  }
}

