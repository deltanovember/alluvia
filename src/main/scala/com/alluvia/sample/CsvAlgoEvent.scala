package com.alluvia.sample


import scala.math.{abs, log}
import collection.mutable.{ListBuffer, HashMap}
import com.alluvia.algo.{EventAlgo}
import java.util.{HashSet, Date}
import com.alluvia.types.market._
import com.alluvia.tools.exchange.OrderBook
import com.alluvialtrading.fix.OrderSide
import com.alluvia.fix.Heartbeat
import fix.{FIXOrderCancelReject, FIXReject, FIXCancel, FIXNew}

trait CsvAlgoEvent
  extends EventAlgo {

  def getServerRoutingKey = "test"
  override def onFIXHeartbeat {
    println("CSV heartbeat received")
  }
  override def onFIXCancel(f: FIXCancel) {
    println("cancel received", f, new Date)
  }
  override def onFIXNew(f: FIXNew) {
    println("new received", f, f.orderID, f.clOrdID, getAnyOrderByID(extractFIX(f.clOrdID)).getSymbol)
  }
  override def onFIXOrderCancelReject(f: FIXOrderCancelReject) {
    println("cancel reject received", f, new Date)
  }
  override def onFIXReject(f: FIXReject) {
    val order = getAnyOrderByID(extractFIX(f.clOrdID))
    println("reject received", f.rejectReason, f.clOrdID, order.getSymbol)
  }
  override def onStart(s: Start) {

   val order = submitLimitOrder(OrderSide.SELL, "ALL.AX", 20, 20)

//    val order2 = createOrderWithoutSend(OrderSide.BUY, "CBA.AX", 20.05, 20)
//   // println("orderid", order.getID, order.getOriginalID)
//    Thread.sleep(5000)
//    //cancelOrder(order)
//    amendOrder(order, order2)
//
//        val order3 = createOrderWithoutSend(OrderSide.BUY, "CBA.AX", 19.5, 20)
//   // println("orderid", order.getID, order.getOriginalID)
//    Thread.sleep(5000)
//    amendOrder(order2, order3)
    
  }

}
