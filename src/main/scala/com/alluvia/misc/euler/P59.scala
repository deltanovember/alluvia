package com.alluvia.misc.euler

import io.Source
import collection.mutable.HashSet

object P59 {

  def main(args: Array[String]) {
    val dir = "src\\main\\scala\\com\\alluvia\\misc\\euler\\"
    val dict = new HashSet[String]
    val letters = 32 :: (97 to 122).toList ::: (65 to 90).toList //.map(x => x.toChar)
    def allLetters(list: List[Char]) = list.map(x => x.toInt).filter(x => letters.contains(x)).length > list.length - 100

    val combos = (97 to 122).combinations(3).toList.map(x => x.toList)
    val perms = new HashSet[List[Int]]
    combos.foreach(x => x.permutations.foreach(y => perms.add(y)))

    for (line <- Source.fromFile(dir + "cipher1.txt").getLines()) {
      val encoded = line.split(",").map(x => x.toInt).toList
      perms.toList.foreach {
        x =>
          val decoded = applyCipher(encoded, x)
          if (allLetters(decoded)) {
            println(decoded.map(x => x.toInt).sum)
          }

      }
    }

    def applyCipher(encoded: List[Int], key: List[Int]) = (encoded zip (Stream continually key).flatten).map(x => x._1 ^ x._2).map(x => x.toChar)

  }

}