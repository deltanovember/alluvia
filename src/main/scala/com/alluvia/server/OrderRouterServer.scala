package com.alluvia.server

import com.rabbitmq.client.{QueueingConsumer, Channel, Connection, ConnectionFactory}
import org.slf4j.{LoggerFactory, Logger}
import quickfix._
import com.alluvialtrading.fix.Order
import com.alluvia.patterns.Observer
import com.alluvia.types.ObservedEventPump
import field.ExecType
import java.io._
import com.alluvia.fix.{Heartbeat, FixApplication, FixProcessor}
import collection.mutable.HashMap
import com.alluvia.algo.Toolkit

object OrderRouterServer extends Observer[Any] with Toolkit {

  // Rabbit exchange
  val EXCHANGE_NAME = "order_routing"
  // Client message keys
  val names = List("exit", "solo", "sunset", "test")

  // FIX
  val log: Logger = LoggerFactory.getLogger(classOf[FixProcessor])
  var inputStream: InputStream = new FileInputStream("fix.cfg")
  var settings: SessionSettings = new SessionSettings(inputStream)
  var messageStoreFactory: MessageStoreFactory = new FileStoreFactory(settings)

  var logHeartbeats: Boolean = System.getProperty("logHeartbeats", "true").toBoolean
  var logFactory: LogFactory = new ScreenLogFactory(true, true, true, logHeartbeats)
  var messageFactory: MessageFactory = new DefaultMessageFactory
  val application = new FixApplication(this)
  val initiator = new ThreadedSocketInitiator(application, messageStoreFactory, settings, logFactory, messageFactory)

  var totalOrders = 0
  val ORDER_LIMIT = 150
  val VALUE_LIMIT = 25000

  // FIX login
  login

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

  // Map order IDs to routing keys using ISIN
  val orderToKey = new HashMap[String, String]

  // Try using pure IDs as well
  val orderID = new HashMap[String, String]

  if (!new File("data").exists()) new File("data").mkdir()

  def login() {
    try {

      println("FIX login")
      // Log in
      initiator.start()
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

  def processOrder(order: Order) = {
    totalOrders += 1
    order.setSessionID(application.getSessionID())

    printcsv("data\\serverorders.csv", order.getMessage, order.getID ,order.getSecurityID, order.getSymbol, order.getLimit, order.getQuantity)
    //println(order.getSymbol, order.getLimit, order.getMessage, order.getOriginalID, order.generateID(), order.getSecurityID)
    order.getMessage match {
      case "SEND" => application.send(order)
      case "CANCEL" =>
        // Hack to make sure initial order went through
        Thread.sleep(50)
        application.cancel(order)
      case _ => println("orderrouterserver", "unknown")
    }
  }
  private def processFIXExecutionReport(message: quickfix.fix42.ExecutionReport) {

    val execType = message.getExecType.getValue
    val isin = message.getSecurityID.toString.split("=")(1)
    if (!orderToKey.contains(isin)) {
      printcsv("data\\servererror.csv", isin, "Key not found")
      return
    }
    val key = orderToKey(isin) + "client"
    execType match {
      case ExecType.NEW =>
        //println("Received NEW from IG", message.getClOrdID,isin)
        channel.basicPublish(EXCHANGE_NAME, key, null, serialize(message))
      case ExecType.PARTIAL_FILL => channel.basicPublish(EXCHANGE_NAME, key, null, serialize(message))
      case ExecType.FILL => channel.basicPublish(EXCHANGE_NAME, key, null, serialize(message))
      case ExecType.CANCELED =>
        printcsv("data\\cancel.csv", message.getOrderID, message.getClOrdID, message.getSecurityID)
        channel.basicPublish(EXCHANGE_NAME, key, null, serialize(message))
      case ExecType.REPLACE => print("replace")
      case ExecType.REJECTED =>
        printcsv("data\\sunsetreject.csv", message.getOrderID, message.getClOrdID, message.getSecurityID)
        channel.basicPublish(EXCHANGE_NAME, key, null, serialize(message))
      case _ => print(execType)
    }

  }
  def receiveUpdate(event: Any) {
    printcsv("data\\server_inbound_log.csv", event)
    event match {
      // send to client
      case pump: ObservedEventPump =>
        println("Server received", pump.currentEvent)
        pump.currentEvent match {
          case h: com.alluvia.fix.Heartbeat => println("heartbeat")
          case event: quickfix.fix42.ExecutionReport => processFIXExecutionReport(event)
          case event: quickfix.fix42.OrderCancelReject =>
            val origID = event.getOrigClOrdID.toString.split("=")(1)
            if (orderID.contains(origID)) {
              val key = orderID(origID) + "client"
              channel.basicPublish(EXCHANGE_NAME, key, null, serialize(pump.currentEvent))
              printcsv("data\\server_inbound_log.csv", "CancelReject", origID)
            }
            else printcsv("data\\server_inbound_log.csv", "Cancel not found", origID)

            printcsv("data\\server_inbound_log.csv", origID)
          case _ => channel.basicPublish(EXCHANGE_NAME, "solo" + "client", null, serialize(pump.currentEvent))
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
          orderID.put(o.getID, delivery.getEnvelope.getRoutingKey)
          processOrder(o)
          println("received order from " + delivery.getEnvelope.getRoutingKey, o.getOriginalID)
        case (oldOrder: Order, newOrder: Order) => println("amend", oldOrder.getLimit, newOrder.getLimit)
        oldOrder.setSessionID(application.getSessionID())
        newOrder.setSessionID(application.getSessionID())
        application.replace(oldOrder, newOrder)
        case h: Heartbeat => println("received heartbeat from " + delivery.getEnvelope.getRoutingKey)
      }

      bis.close();
      in.close();

    }
  }
}