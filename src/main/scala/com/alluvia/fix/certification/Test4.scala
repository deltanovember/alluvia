package com.alluvia.fix.certification

import com.alluvia.fix.FixProcessor
import com.alluvialtrading.fix.OrderSide._

// Place a New Order

abstract class Test4 extends FixProcessor {
  override def performCertification() {
    // Place
    val order = sendLimitOrder(BUY, "BARC.L", 16145.0, 201)
    Thread.sleep(10000)

    // Amend
    val order2 = createLimitOrder(BUY, "BARC.L", 16145.0, 30)
    replaceOrder(order, order2)
    Thread.sleep(15000)

    // Amend
    val order3 = createLimitOrder(BUY, "BARC.L", 16155.0, 30)
    replaceOrder(order2, order3)
    Thread.sleep(10000)

    cancelOrder(order3)
    Thread.sleep(10000)

    println("exiting")
    System.exit(0)
  }
}