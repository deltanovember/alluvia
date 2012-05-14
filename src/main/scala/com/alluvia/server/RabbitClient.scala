package com.alluvia.server

import com.rabbitmq.client.{QueueingConsumer, Channel, Connection, ConnectionFactory}
import com.alluvialtrading.fix.{OrderTIF, OrderType, Order, OrderSide}
import java.io.{ObjectInputStream, ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream}
import com.alluvia.fix.Heartbeat

object RabbitClient {

  private final val EXCHANGE_NAME: String = "order_routing"
  val factory: ConnectionFactory = new ConnectionFactory
  factory.setHost("alluvial-db.cmcrc.com")
  val connection: Connection = factory.newConnection
  val channel: Channel = connection.createChannel
  channel.exchangeDeclare(EXCHANGE_NAME, "direct")
  val key: String = "fix"

  def main(argv: Array[String]): Unit = {


    val order = generateOrder(OrderSide.BUY, "BHP", 30.5, 500)
    val bos = new ByteArrayOutputStream();
    val out = new ObjectOutputStream(bos);
    val start = System.currentTimeMillis()
    out.writeObject(order);

    val bytes = bos.toByteArray()


    channel.basicPublish(EXCHANGE_NAME, key, null, bytes)
    out.close();
    bos.close();
    System.out.println(" [x] Sent '" + key + "':'" + order + "'")

    val queueName: String = channel.queueDeclare.getQueue
    val consumer: QueueingConsumer = new QueueingConsumer(channel)
    channel.basicConsume(queueName, true, consumer)
    channel.queueBind(queueName, EXCHANGE_NAME, "clientonly")

    while (true) {
      val delivery: QueueingConsumer.Delivery = consumer.nextDelivery
      //val receivingMessage: String = new String(delivery.getBody)
      // val routingKey: String = delivery.getEnvelope.getRoutingKey
      //System.out.println(" [x] Received '" + routingKey + "':'" + receivingMessage + "'")
      val bis = new ByteArrayInputStream(delivery.getBody);
      val in = new ObjectInputStream(bis);
      val o = in.readObject();
      o match {
        case o: Heartbeat => println("Heartbeat received")
        case _ => println("Unknown", o)
      }

      bis.close();
      in.close();
    }

  }

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

    order

  }

}
