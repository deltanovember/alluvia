package com.alluvia.fix.certification

import com.alluvia.fix.FixProcessor
import com.alluvialtrading.fix.OrderSide._

// Place a New Order

abstract class Test6 extends FixProcessor {
  override def performCertification() {
    // Place
    val order = sendLimitOrder(SELL, "BARC.L", 16145.0, 15)
    Thread.sleep(5000)
    System.exit(0)
  }
}