package com.alluvia.server

import com.alluvialtrading.fix.{OrderTIF, OrderType, Order, OrderSide}
import java.io.{ObjectOutputStream, ByteArrayOutputStream}
import com.rabbitmq.client.{Channel, Connection, ConnectionFactory}

object DummyFIXClient extends App {

  // Rabbit setup
  private final val EXCHANGE_NAME: String = "order_routing"
  val key: String = "test"
  val factory: ConnectionFactory = new ConnectionFactory
  factory.setHost("alluvial-db.cmcrc.com")
  val connection: Connection = factory.newConnection
  val channel: Channel = connection.createChannel
  channel.exchangeDeclare(EXCHANGE_NAME, "direct")


  generateOrder(OrderSide.BUY, "BHP", 10.5, 500)
  println("here")


  def generateOrder(side: OrderSide, symbol: String, limit: Double, quantity: Int): Order = {

    val order: Order = new Order

    order.setSide(side)
    order.setType(OrderType.LIMIT)
    order.setTIF(OrderTIF.DAY)
    order.setSymbol(symbol)
    order.setLimit(limit)
    order.setQuantity(quantity)
    order.setOpen(order.getQuantity)
    order.setIdSource("4")
    val orderType: OrderType = order.getType


    if (orderType == OrderType.STOP || orderType == OrderType.STOP_LIMIT)
      order.setStop(200.0)

    order.setMessage("CANCEL")
    println(System.currentTimeMillis())
    for (i <- 0 to 100000) {
          val bos = new ByteArrayOutputStream();
    val out = new ObjectOutputStream(bos);
    val start = System.currentTimeMillis()
    out.writeObject(order);

    val bytes = bos.toByteArray()


    channel.basicPublish(EXCHANGE_NAME, key, null, bytes)
          out.close();
    bos.close();
            val rnd = new scala.util.Random
      val range = 5 to 10
      Thread.sleep(range(rnd.nextInt(range length)))
    }

    println(System.currentTimeMillis())


    order

  }
}