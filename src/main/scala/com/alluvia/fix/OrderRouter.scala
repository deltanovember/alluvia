/**
 * FIX order router
 */

package com.alluvia.fix

import com.alluvialtrading.fix.{OrderTIF, OrderType, Order, OrderSide}
import com.alluvia.markets.Market
import org.slf4j.{LoggerFactory, Logger}
import java.io.{FileInputStream, InputStream}
import quickfix._
import com.alluvialtrading.fix.OrderSide._

abstract class OrderRouter extends Market {

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

  }
  catch {
    case e: Exception => {
      log.error("Logon failed", e)
    }
  }

  def cancelOrder(order: Order) {
    application.cancel(order)
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