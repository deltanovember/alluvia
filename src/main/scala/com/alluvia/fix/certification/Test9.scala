package com.alluvia.fix.certification

import com.alluvia.fix.FixProcessor
import com.alluvialtrading.fix.OrderSide._

// Place a New Order

abstract class Test9 extends FixProcessor {
  override def performCertification() {
    // Place
    val order = sendLimitOrder(SELL, "BARC.L", 16145.0, 30)
    Thread.sleep(10000)
    cancelOrder(order)
    Thread.sleep(5000)
    System.exit(0)
  }
}