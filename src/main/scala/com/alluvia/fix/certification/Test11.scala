package com.alluvia.fix.certification

import com.alluvia.fix.FixProcessor
import com.alluvialtrading.fix.OrderSide._

// Place a New Order which is accepted (New) then receive an unsolicited Cancel



abstract class Test11 extends FixProcessor {
  override def performCertification() {
    // Place
    val order = sendLimitOrder(SELL, "BARC.L", 16145.0, 205)
    Thread.sleep(5000000)
    System.exit(0)
  }
}