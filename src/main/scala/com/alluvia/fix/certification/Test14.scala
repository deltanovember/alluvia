package com.alluvia.fix.certification

import com.alluvia.fix.FixProcessor
import com.alluvialtrading.fix.OrderSide._

// Arbitrary Rejection of Order

abstract class Test14 extends FixProcessor {
  override def performCertification() {
    // Place
    val order = sendLimitOrder(SELL, "BARC.L", 16145.0, 1)
    Thread.sleep(5000)
    System.exit(0)
  }
}