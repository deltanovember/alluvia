package com.alluvia.misc.euler

import java.math.BigInteger

/**
 * Created by IntelliJ IDEA.
 * User: owner
 * Date: 29/10/11
 * Time: 1:02 PM
 * To change this template use File | Settings | File Templates.
 */

object P16 extends App {

  println(new BigInteger("2").pow(1000).toString.toCharArray.map(_.toString.toInt).sum)
}