package com.alluvia.misc.euler

import com.alluvia.algo.Toolkit


object P10 extends App with Tools with Toolkit {
ps.takeWhile(x => x <2000000).foreach(y => printcsv("P10.csv", y))
 // println(ps.takeWhile(x => x <2000000).sum)
}