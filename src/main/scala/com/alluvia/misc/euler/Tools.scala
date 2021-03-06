package com.alluvia.misc.euler


trait Tools {

    lazy val ps: Stream[Int] = 2 #:: Stream.from(3).filter(i =>  ps.takeWhile(j => j * j <= i).forall(i % _ > 0))

}