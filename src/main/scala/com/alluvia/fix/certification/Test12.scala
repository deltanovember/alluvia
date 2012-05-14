package com.alluvia.fix.certification

import com.alluvia.fix.FixProcessor
import com.alluvialtrading.fix.OrderSide._

// Enter a Duplicate Order which will be rejected


abstract class Test12 extends FixProcessor {
  override def performCertification() {
    // Place
    val order = sendLimitOrder(SELL, "BARC.L", 16145.0, 205)
    Thread.sleep(5000)
    application.send(order)
    Thread.sleep(5000)
    System.exit(0)
  }
}