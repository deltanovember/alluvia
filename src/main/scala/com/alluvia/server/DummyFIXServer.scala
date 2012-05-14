package com.alluvia.server

import java.io.{ObjectInputStream, ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream}
import com.rabbitmq.client.{Channel, Connection, ConnectionFactory, QueueingConsumer}
import com.alluvia.types.market.{DayEnd, Control}
import com.alluvialtrading.fix.Order

object DummyFIXServer {

  val EXCHANGE_NAME = "order_routing"
  val names = List("fix")

  // Rabbit setup
  var factory: ConnectionFactory = new ConnectionFactory
  factory.setHost("alluvial-db.cmcrc.com")
  val connection: Connection = factory.newConnection
  val channel: Channel = connection.createChannel
  channel.exchangeDeclare(EXCHANGE_NAME, "direct")

  var queueName: String = channel.queueDeclare.getQueue

  for (name <- names) {
    channel.queueBind(queueName, EXCHANGE_NAME, name)
  }

  var consumer: QueueingConsumer = new QueueingConsumer(channel)
  channel.basicConsume(queueName, true, consumer)

  def main(args: Array[String]) {
    var counter = 0
    System.out.println(" [*] Waiting for messages.")
    while (true) {
      var delivery: QueueingConsumer.Delivery = consumer.nextDelivery
      var message: String = new String(delivery.getBody)
      var routingKey: String = delivery.getEnvelope.getRoutingKey
      //System.out.println(" [x] Received " + routingKey)
      val bis = new ByteArrayInputStream(delivery.getBody);
      val in = new ObjectInputStream(bis);
      val o = in.readObject();
      o match {
        case o: Order => //println(o.getSide, o.getSymbol, o.getLimit, o.getQuantity)
      }

      bis.close();
      in.close();
      counter += 1
      if (counter % 10000 == 0) println(System.currentTimeMillis())

    }
  }


}