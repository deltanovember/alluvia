package com.alluvia.algo

import com.alluvia.markets.Market
import datasource.LiveData
import com.alluvialtrading.fix.{OrderTIF, OrderType, Order, OrderSide}
import com.alluvia.patterns.Observer
import com.alluvia.types.ObservedEventPump
import com.rabbitmq.client.{QueueingConsumer, Channel, Connection, ConnectionFactory}
import java.io.{ObjectInputStream, ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream}
import com.alluvia.fix.Heartbeat
import java.util.Date

trait OrderRouterClient extends Market with EventAlgo {
  self: LiveData =>

  var totalOrders = 0
  val ORDER_LIMIT = 80
  val VALUE_LIMIT = 25000

  private final val EXCHANGE_NAME: String = "order_routing"
  val factory: ConnectionFactory = new ConnectionFactory
  factory.setHost("alluvial-db.cmcrc.com")
  val connection: Connection = factory.newConnection
  val channel: Channel = connection.createChannel
  channel.exchangeDeclare(EXCHANGE_NAME, "direct")

  // Actor to receive FIX messages from server
  (new FixReceivingActor(this)).start()

  override def amendOrder(oldOrder: Order, newOrder: Order) {
    sendAmendToServer(oldOrder, newOrder)
  }

  override def cancelOrder(order: Order) {
    order.setMessage("CANCEL")
    sendOrderToServer(order)
  }

  override def createOrderWithoutSend(side: OrderSide, symbol: String, limit: Double, quantity: Int): Order = {
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

    order
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
    order.setMessage("SEND")
    order.setQuantity(quantity)
    order.setOpen(order.getQuantity)
    order.setSecurityID(getISIN(symbol))
    order.setIdSource("4")
    order.setSecurityExchange(getSecurityExchange)
    order.setCurrency(getCurrency)
    val orderType: OrderType = order.getType


    if (orderType == OrderType.STOP || orderType == OrderType.STOP_LIMIT)
      order.setStop(200.0)
    sendOrderToServer(order)

    order

  }

  // Unique key for routing FIX messages
  // To clientC
  def getClientRoutingKey = getServerRoutingKey + "client"

  // To server
  def getServerRoutingKey: String


  def sendAmendToServer(oldOrder: Order, newOrder: Order) {
    // Route to server
    val bos = new ByteArrayOutputStream();
    val out = new ObjectOutputStream(bos);
    out.writeObject((oldOrder, newOrder));

    val bytes = bos.toByteArray()

    channel.basicPublish(EXCHANGE_NAME, getServerRoutingKey, null, bytes)
    out.close();
    bos.close();
    System.out.println(new Date, " [x] Sent '" + getServerRoutingKey + "':'" + oldOrder + newOrder + "'")
    // End routing
  }

  def sendOrderToServer(order: Order) {
    // Route to server
    val bos = new ByteArrayOutputStream();
    val out = new ObjectOutputStream(bos);
    out.writeObject(order);

    val bytes = bos.toByteArray()

    channel.basicPublish(EXCHANGE_NAME, getServerRoutingKey, null, bytes)
    out.close();
    bos.close();
    System.out.println(new Date, " [x] Sent '" + getServerRoutingKey + "':'" + order + "'")
    // End routing
  }

  class FixReceivingActor(observer: Observer[Any]) extends scala.actors.Actor {
    // Used to passively push FIX messages
    val eventPump = new ObservedEventPump
    eventPump.addObserver(observer)

    // Rabbit
    private final val EXCHANGE_NAME: String = "order_routing"
    val factory: ConnectionFactory = new ConnectionFactory
    factory.setHost("alluvial-db.cmcrc.com")
    val connection: Connection = factory.newConnection
    val channel: Channel = connection.createChannel
    channel.exchangeDeclare(EXCHANGE_NAME, "direct")
    val queueName: String = channel.queueDeclare.getQueue
    val consumer: QueueingConsumer = new QueueingConsumer(channel)
    channel.basicConsume(queueName, true, consumer)
    channel.queueBind(queueName, EXCHANGE_NAME, getClientRoutingKey)

    def act() {
      while (true) {
        val delivery: QueueingConsumer.Delivery = consumer.nextDelivery
        val bis = new ByteArrayInputStream(delivery.getBody);
        val in = new ObjectInputStream(bis);
        val o = in.readObject();
        o match {
          case o: Heartbeat => eventPump.addEvent(o)
          case er: quickfix.fix42.ExecutionReport =>
            //println("Client rec ER")
            eventPump.addEvent(er)
          case event: quickfix.fix42.OrderCancelReject => eventPump.addEvent(event)
          case _ => println("Unknown in order router client", o.getClass.getName)
        }

        bis.close();
        in.close();
      }
    }
  }

}