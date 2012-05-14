package com.alluvia.fix

import java.io.{FileInputStream, InputStream}
import org.slf4j.{LoggerFactory, Logger}
import com.alluvia.markets._
import com.alluvialtrading.fix.Order
import com.alluvialtrading.fix.OrderSide
import com.alluvialtrading.fix.OrderTIF
import com.alluvialtrading.fix.OrderType
import OrderSide.BUY
import quickfix._
object FixProcessor {
  def main(args: Array[String]) {
    new FixProcessor with LSE
  }
}

abstract class FixProcessor extends Market {

  val log: Logger = LoggerFactory.getLogger(classOf[FixProcessor])
  var inputStream: InputStream = new FileInputStream("fix.cfg")
  var settings: SessionSettings = new SessionSettings(inputStream)
  var messageStoreFactory: MessageStoreFactory = new FileStoreFactory(settings)

  var logHeartbeats: Boolean = System.getProperty("logHeartbeats", "true").toBoolean
  var logFactory: LogFactory = new ScreenLogFactory(true, true, true, logHeartbeats)
  var messageFactory: MessageFactory = new DefaultMessageFactory
  val application = new FixApplication
  val initiator = new SocketInitiator(application, messageStoreFactory, settings, logFactory, messageFactory)
  try {

    // Log in
    initiator.start
    while (!application.loggedIn) {
      Thread.sleep(100)
    }


    while (true) {

    performCertification()

    }

  }
  catch {
    case e: Exception => {
      log.error("Logon failed", e)
    }
  }

  def cancelOrder(order: Order) {
    application.cancel(order)
  }

  def createLimitOrder(side: OrderSide, symbol: String, limit: Double, quantity: Int): Order = {

    val order: Order = new Order

    order.setSide(side)
    order.setType(OrderType.LIMIT)
    order.setTIF(OrderTIF.DAY)
    order.setSymbol(symbol)
    order.setLimit(limit)
    order.setQuantity(quantity)
    order.setOpen(order.getQuantity)
    order.setSecurityID(getISIN(symbol))
    order.setIdSource("4")
    order.setSecurityExchange(getSecurityExchange)
    order.setCurrency(getCurrency)
    var orderType: OrderType = order.getType


    if (orderType == OrderType.STOP || orderType == OrderType.STOP_LIMIT)
      order.setStop(200.0)

    order.setSessionID(application.getSessionID())
    //application.send(order)
    order

  }

  def performCertification() {
      // Place
      val order = sendLimitOrder(BUY, "BARC.L", 16145.0, 201)
      Thread.sleep(5000)
      System.exit(0)
  }
  def replaceOrder(order: Order, newOrder: Order) {
    application.replace(order, newOrder)
  }
  def sendFakeLimitOrder(side: OrderSide, symbol: String, limit: Double, quantity: Int): Order = {

    val order: Order = new Order

    order.setSide(side)
    order.setType(OrderType.LIMIT)
    order.setTIF(OrderTIF.DAY)
    order.setSymbol(symbol)
    order.setLimit(limit)
    order.setQuantity(quantity)
    order.setOpen(order.getQuantity)
    order.setSecurityID(getISIN(symbol))
    order.setIdSource("4")
    order.setSecurityExchange(getSecurityExchange)
    order.setCurrency(getCurrency)
    var orderType: OrderType = order.getType


    if (orderType == OrderType.STOP || orderType == OrderType.STOP_LIMIT)
      order.setStop(200.0)

    order.setSessionID(application.getSessionID())
    application.send(order)
    order

  }

  def sendLimitOrder(side: OrderSide, symbol: String, limit: Double, quantity: Int): Order = {

    val order: Order = new Order

    order.setSide(side)
    order.setType(OrderType.LIMIT)
    order.setTIF(OrderTIF.DAY)
    order.setSymbol(symbol)
    order.setLimit(limit)
    order.setQuantity(quantity)
    order.setOpen(order.getQuantity)
    order.setSecurityID(getISIN(symbol))
    order.setIdSource("4")
    order.setSecurityExchange(getSecurityExchange)
    order.setCurrency(getCurrency)
    var orderType: OrderType = order.getType


    if (orderType == OrderType.STOP || orderType == OrderType.STOP_LIMIT)
      order.setStop(200.0)

    order.setSessionID(application.getSessionID())
    application.send(order)
    order

  }

}