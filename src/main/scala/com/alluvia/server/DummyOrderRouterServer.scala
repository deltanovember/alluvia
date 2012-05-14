package com.alluvia.server

import com.rabbitmq.client.{QueueingConsumer, Channel, Connection, ConnectionFactory}
import org.slf4j.{LoggerFactory, Logger}
import quickfix._
import field.ExecType
import java.io._
import com.alluvia.fix.{Heartbeat, FixApplication}
import collection.mutable.HashMap
import com.alluvia.patterns.Observer
import com.alluvia.fix.FixProcessor
import com.alluvialtrading.fix.Order
import com.alluvia.algo.Toolkit
import com.alluvia.types.ObservedEventPump

object DummyOrderRouterServer extends Observer[Any] with Toolkit {

  // Rabbit exchange
  val EXCHANGE_NAME = "order_routing"
  // Client message keys
  val names = List("solo", "sunset", "test")

  var totalOrders = 0
  val ORDER_LIMIT = 150
  val VALUE_LIMIT = 25000

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

  // Map order IDs to routing keys
  val orderToKey = new HashMap[String, String]

  def processOrder(order: Order) = {
    totalOrders += 1

    println(order.getSymbol, order.getLimit, order.getMessage, order.getOriginalID, order.generateID(), order.getSecurityID)
    order.getMessage match {
      case "SEND" =>
        Thread.sleep(10000)
        println(totalOrders)
      case "CANCEL" => println(totalOrders)
      case _ => println("unknown")
    }
  }
  private def processFIXExecutionReport(message: quickfix.fix42.ExecutionReport) {

    val execType = message.getExecType.getValue
    val isin = message.getSecurityID.toString.split("=")(1)
    val key = orderToKey(isin) + "client"
    execType match {
      case ExecType.NEW =>
        //println("Received NEW from IG", message.getClOrdID,isin)
        channel.basicPublish(EXCHANGE_NAME, key, null, serialize(message))
      case ExecType.PARTIAL_FILL => channel.basicPublish(EXCHANGE_NAME, key, null, serialize(message))
      case ExecType.FILL => channel.basicPublish(EXCHANGE_NAME, key, null, serialize(message))
      case ExecType.CANCELED =>
        printcsv("cancel.csv", message.getOrderID, message.getClOrdID, message.getSecurityID)
        channel.basicPublish(EXCHANGE_NAME, key, null, serialize(message))
      case ExecType.REPLACE => print("replace")
      case ExecType.REJECTED => print("rejected")
      case _ => print(execType)
    }

  }
  def receiveUpdate(event: Any) {
    event match {
      // send to client
        case pump: ObservedEventPump =>
        println("Server received", pump.currentEvent)
        pump.currentEvent match {
          case h: com.alluvia.fix.Heartbeat => println("heartbeat")
          case event: quickfix.fix42.ExecutionReport => processFIXExecutionReport(event)
          case _ =>  channel.basicPublish(EXCHANGE_NAME, "solo" + "client", null, serialize(pump.currentEvent))
        }

    }
  }

  def serialize(o: Any) = {
    val bos = new ByteArrayOutputStream();
    val out = new ObjectOutputStream(bos);
    out.writeObject(o);
    out.close();
    bos.close();
    bos.toByteArray()
  }

  def main(args: Array[String]) {

    System.out.println(" [*] Waiting for messages.")

    while (true) {
      val delivery: QueueingConsumer.Delivery = consumer.nextDelivery

      val bis = new ByteArrayInputStream(delivery.getBody);
      val in = new ObjectInputStream(bis);
      val o = in.readObject();
      o match {
        case o: Order =>
          orderToKey.put(o.getSecurityID, delivery.getEnvelope.getRoutingKey)
          processOrder(o)
          println("received order from " + delivery.getEnvelope.getRoutingKey, o.getOriginalID)
        case h: Heartbeat => println("received heartbeat from " + delivery.getEnvelope.getRoutingKey)
      }

      bis.close();
      in.close();

    }
  }
}