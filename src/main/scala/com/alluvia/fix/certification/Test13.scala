package com.alluvia.fix.certification

import com.alluvia.fix.FixProcessor
import com.alluvialtrading.fix.OrderSide._

// Enter an order with an Incorrect SecurityId which will be rejected

abstract class Test13 extends FixProcessor {
  override def performCertification() {
    // Place
    val order = sendFakeLimitOrder(SELL, "BARC.L", 16145.0, 205)
    Thread.sleep(5000)
    System.exit(0)
  }
}