package com.alluvia.fix.certification

import com.alluvia.fix.FixProcessor
import com.alluvialtrading.fix.OrderSide._

// Place a New Order

abstract class Test2 extends FixProcessor {
  override def performCertification() {
    // Place
    val order = sendLimitOrder(BUY, "BARC.L", 16145.0, 201)
    Thread.sleep(10000)

    // Cancel
    cancelOrder(order)
    Thread.sleep(10000)
    System.exit(0)
  }
}