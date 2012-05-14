package com.alluvia.misc.euler

import scala.Predef._
import collection.mutable.{ListBuffer, HashSet, HashMap}

object P61 extends App {

  val list = 1 to 1000
  def triList = list.map(x => ((n: Int) => n * (n + 1) / 2)(x)).filter(x => x >= 1000 && x <= 9999).toSet
  def squList = list.map(x => ((n: Int) => n * n)(x)).filter(x => x >= 1000 && x <= 9999).toSet
  def penList = list.map(x => ((n: Int) => n * (3 * n - 1) / 2)(x)).filter(x => x >= 1000 && x <= 9999).toSet
  def hexList = list.map(x => ((n: Int) => n * (2 * n - 1))(x)).filter(x => x >= 1000 && x <= 9999).toSet
  def hepList = list.map(x => ((n: Int) => n * (5 * n - 3) / 2)(x)).filter(x => x >= 1000 && x <= 9999).toSet
  val octList = list.map(x => ((n: Int) => n * (3 * n - 2))(x)).filter(x => x >= 1000 && x <= 9999).toSet
  val all = List(triList, squList, penList, hexList, hepList, octList).permutations

  def getCyclic(n: Int) = n.toString.takeRight(2).toInt * 100

  all.foreach {
    i => val combo = getCombo(i)
    if (combo._1 > 0) {
      println(combo, combo._1 + combo._2 + combo._3 + combo._4 + combo._5 + combo._6)
    }
  }

  def getCombo(all: List[Set[Int]]): (Int, Int, Int, Int, Int, Int) = {

    var combo = (0, 0, 0, 0, 0, 0)
    all(0).foreach {
      i => val cyclic1 = getCyclic(i)
      for ( j <- cyclic1 to cyclic1 + 99) {
        if (all(1).contains(j)) {
          val cyclic2 = getCyclic(j)
          for (k <- cyclic2 to cyclic2 + 99) {
            if (all(2).contains(k)) {
              val cyclic3 = getCyclic(k)
              for (l <- cyclic3 to cyclic3 + 99) {
                if (all(3).contains(l)) {
                  val cyclic4 = getCyclic(l)

                  for (m <- cyclic4 to cyclic4 + 99) {
                    if (all(4).contains(m)) {

                      val cyclic5 = getCyclic(m)
                      for (n <- cyclic5 to cyclic5 + 99) {
                        if (all(5).contains(n)) {
                                            //      println(i, j, k, l, m, n)
                        if (getCyclic(n).toString.take(2) == i.toString.take(2)) {

                         // println("here")
                          combo = (i, j, k, l, m, n)
                        }
                        }

                      }
                    }

                  }

                }
              }

            }
          }
        }
      }
    }
    return combo
  }

}