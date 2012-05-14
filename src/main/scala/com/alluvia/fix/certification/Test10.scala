package com.alluvia.fix.certification

import com.alluvia.fix.FixProcessor
import com.alluvialtrading.fix.OrderSide._

// Place a New Order which is subsequently filled then attempt to Cancel it, receive an Order Cancel Reject


abstract class Test10 extends FixProcessor {
  override def performCertification() {
    // Place
    val order = sendLimitOrder(SELL, "MLB.AX", 1.2, 1)
    Thread.sleep(5000)
    cancelOrder(order)
    Thread.sleep(5000)
    System.exit(0)
  }
}