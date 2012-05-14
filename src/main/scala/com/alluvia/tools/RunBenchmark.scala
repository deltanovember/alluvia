package com.alluvia.tools

import com.alluvia.markets.ASX

/**
 * Created by IntelliJ IDEA.
 * User: owner
 * Date: 28/07/11
 * Time: 11:58 AM
 * To change this template use File | Settings | File Templates.
 */

object RunBenchmark {
  def main(args: Array[String]) {
    new BenchmarkGenerator with ASX
  }

}