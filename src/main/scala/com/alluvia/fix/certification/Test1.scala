package com.alluvia.fix.certification

import com.alluvia.fix.FixProcessor
import com.alluvialtrading.fix.OrderSide._

// Place a New Order

abstract class Test1 extends FixProcessor {
  override def performCertification() {
    // Place
    val order = sendLimitOrder(SELL, "BARC.L", 16145.0, 201)
    Thread.sleep(10000)
    System.exit(0)
  }
}