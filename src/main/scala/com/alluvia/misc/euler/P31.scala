package com.alluvia.misc.euler

import com.alluvia.algo.Toolkit

object P31 extends App with Toolkit {

  var count = 0
  for (a<-0 to 200) {
    for (b <- 0 to 100) {
      for (c <- 0 to 40) {
        for (d <- 0 to 20) {
          for (e <- 0 to 10) {
            for (f <- 0 to  4) {
              for (g <- 0 to 2) {
                for (h <- 0 to 1) {
                  if (a + 2*b + 5*c + 10*d + 20*e + 50*f + 100*g + 200*h == 200) count +=1
                  //printcsv("31.csv", a, b, c, d, e, f, g, h)
                }
              }
            }
          }
        }
      }
    }
  }
  println(count)

}