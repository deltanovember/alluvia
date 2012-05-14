package com.alluvia.algo

/**
 * Be very careful this WILL trade
 */

import com.alluvialtrading.fix.{OrderTIF, OrderType, Order, OrderSide}
import datasource.{LiveData, Iress}
import org.slf4j.{LoggerFactory, Logger}
import java.io.{FileInputStream, InputStream}
import com.alluvia.fix.{FixApplication, FixProcessor}
import quickfix._
import com.alluvia.markets.Market

trait FIX extends Market with EventAlgo {
   self: LiveData =>

  // FIX
  val log: Logger = LoggerFactory.getLogger(classOf[FixProcessor])
  var inputStream: InputStream = new FileInputStream("fix.cfg")
  var settings: SessionSettings = new SessionSettings(inputStream)
  var messageStoreFactory: MessageStoreFactory = new FileStoreFactory(settings)

  var logHeartbeats: Boolean = System.getProperty("logHeartbeats", "true").toBoolean
  var logFactory: LogFactory = new ScreenLogFactory(true, true, true, logHeartbeats)
  var messageFactory: MessageFactory = new DefaultMessageFactory
  val application = new FixApplication(this)
  val initiator = new SocketInitiator(application, messageStoreFactory, settings, logFactory, messageFactory)

  var totalOrders = 0
  val ORDER_LIMIT = 60
  val VALUE_LIMIT = 25000

  login

  override def cancelOrder(order: Order) {
    application.cancel(order)
  }


  override def login() {
    try {

      println("FIX login")
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
  }

  override def generateOrder(side: OrderSide, symbol: String, limit: Double, quantity: Int): Order = {

    // FIX check
    totalOrders += 1

    println("Total orders: " + totalOrders)
    if (totalOrders > ORDER_LIMIT) {
      println("Critical: order limit exceeded. Exitng")
      System.exit(0)
    }

    if (limit * quantity / getBrokerCurrencyMultiplier > VALUE_LIMIT) {
      println("Critical: value limit exceeded. Exiting")
      System.exit(0)
    }

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
    val orderType: OrderType = order.getType


    if (orderType == OrderType.STOP || orderType == OrderType.STOP_LIMIT)
      order.setStop(200.0)

    order.setSessionID(application.getSessionID())
    application.send(order)

    order

  }

}