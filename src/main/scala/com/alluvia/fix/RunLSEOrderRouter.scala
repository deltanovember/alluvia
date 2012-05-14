package com.alluvia.fix

import com.alluvialtrading.fix.OrderSide._
import com.alluvia.markets.ASX

object RunLSEOrderRouter {

  def main(args: Array[String]) {
    val orderRouter = new OrderRouter with ASX
    Thread.sleep(5000)
    val order = orderRouter.sendLimitOrder(BUY, "MLB.AX", 1.1, 10)

    Thread.sleep(5000)
    orderRouter.cancelOrder(order)
    Thread.sleep(5000)
  }
}