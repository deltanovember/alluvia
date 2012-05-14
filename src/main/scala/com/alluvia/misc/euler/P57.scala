package com.alluvia.misc.euler

/**
 * \P 20
gcd:{[x;y] $[0f~y;:x;:gcd[y;x mod y]]; };
a:{(x+2*y;y)} / addtwo
r:{(x%g;y%g:gcd[x;y])} / reduce
b:{r[a[x;y][0];a[x;y][1]]}
i:{{(b[x[0];x[1]][1];b[x[0];x[1]][0])}/[x;(1f;2f)]}
c: 1+ til 1000
d: {i[x]} each c
e:{(x[0]+x[1];x[1])} each d
count e where {(count string x[0])>(count string x[1])} each e
 */

object P57 extends App {

  def a(x: BigInt, y: BigInt) = (x+BigInt(2)*y, y)
  def b(x: BigInt, y: BigInt) = (a(x,y)._1, a(x,y)._2)
  def i(x: BigInt, y: BigInt, counter: Int):(BigInt, BigInt) = {
    if (counter==0) {
      return (b(x,y)._1+b(x,y)._2, b(x,y)._1)
    }
    i(b(x,y)._2, b(x,y)._1, counter-1)
  }
  val range = 1 to 1000
  println(range.map(x => i(BigInt(1),BigInt(2), x)).filter(x => x._1.toString().length() > x._2.toString().length()).size)

}