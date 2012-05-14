package com.alluvia.misc.euler

import io.Source
import com.alluvialtrading.misc.{Hand, HandEvaluator}

object P54 extends App {

  var counter = 0
  for (line <- Source.fromFile("src\\main\\scala\\com\\alluvia\\misc\\euler\\poker.txt").getLines()) {

    var handEval: HandEvaluator = new HandEvaluator
    val tokens = line.split(" ")
    val h1 = new Hand(tokens.take(5).mkString(" "))
    val h2 = new Hand(tokens.takeRight(5).mkString(" "))
    if (HandEvaluator.rankHand(h1) > HandEvaluator.rankHand(h2)) {
      counter += 1
    }

  }
  println(counter)

}