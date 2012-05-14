package com.alluvia.fix

import com.alluvia.patterns.Observer
import com.alluvia.patterns.Subject

import com.alluvialtrading.fix.Order
import com.alluvialtrading.fix.OrderSide
import com.alluvialtrading.fix.OrderTIF
import com.alluvialtrading.fix.OrderType
import quickfix.field._
import java.util.{Date, HashSet}
import quickfix._
import com.alluvia.types.ObservedEventPump

// Default observer does nothing
class FixApplication(val observer: Observer[Any] = new Observer[Any] {
  def receiveUpdate(subject: Any) = println("default observer doing nothing")
}) extends quickfix.fix42.MessageCracker with quickfix.Application  {

  // Used to passively push FIX messages
  val eventPump = new ObservedEventPump
  eventPump.addObserver(observer)
  
  var loggedIn = false
  private val observableLogon = new ObservableLogon
 // private var orderTableModel: OrderTableModel = null
  private var sessionID: SessionID = null
  private val sideMap = new TwoWayMap
  private var tifMap = new TwoWayMap
  private var typeMap = new TwoWayMap

  // Mappings
  sideMap.put(OrderSide.BUY.getName, new Side(Side.BUY))
  sideMap.put(OrderSide.SELL.getName, new Side(Side.SELL))
  sideMap.put(OrderSide.SHORT_SELL.getName, new Side(Side.SELL_SHORT))
  sideMap.put(OrderSide.SHORT_SELL_EXEMPT.getName, new Side(Side.SELL_SHORT_EXEMPT))
  sideMap.put(OrderSide.CROSS.getName, new Side(Side.CROSS))
  sideMap.put(OrderSide.CROSS_SHORT.getName, new Side(Side.CROSS_SHORT))

  typeMap.put(OrderType.MARKET.getName, new OrdType(OrdType.MARKET))
  typeMap.put(OrderType.LIMIT.getName, new OrdType(OrdType.LIMIT))
  typeMap.put(OrderType.STOP.getName, new OrdType(OrdType.STOP))
  typeMap.put(OrderType.STOP_LIMIT.getName, new OrdType(OrdType.STOP_LIMIT))

  tifMap.put(OrderTIF.DAY.getName, new TimeInForce(TimeInForce.DAY))
  tifMap.put(OrderTIF.IOC.getName, new TimeInForce(TimeInForce.IMMEDIATE_OR_CANCEL))
  tifMap.put(OrderTIF.OPG.getName, new TimeInForce(TimeInForce.AT_THE_OPENING))
  tifMap.put(OrderTIF.GTC.getName, new TimeInForce(TimeInForce.GOOD_TILL_CANCEL))
  tifMap.put(OrderTIF.GTX.getName, new TimeInForce(TimeInForce.GOOD_TILL_CROSSING))

  def addLogonObserver(observer: Observer[Any]) {
    observableLogon.addObserver(observer)
  }

  def cancel(order: Order) {
    var beginString: String = order.getSessionID.getBeginString
    if (beginString == "FIX.4.0") cancel40(order)
    else if (beginString == "FIX.4.1") cancel41(order)
    else if (beginString == "FIX.4.2") cancel42(order)
    return
  }

  def cancel40(order: Order) {
    var id: String = order.generateID
    var message: quickfix.fix40.OrderCancelRequest = new quickfix.fix40.OrderCancelRequest(new OrigClOrdID(order.getID), new ClOrdID(id), new CxlType(CxlType.FULL_REMAINING_QUANTITY), new Symbol(order.getSymbol), sideToFIXSide(order.getSide), new OrderQty(order.getQuantity))
    //orderTableModel.addID(order, id)
    send(message, order.getSessionID)
  }

  def cancel41(order: Order) {
    var id: String = order.generateID
    var message: quickfix.fix41.OrderCancelRequest = new quickfix.fix41.OrderCancelRequest(new OrigClOrdID(order.getID), new ClOrdID(id), new Symbol(order.getSymbol), sideToFIXSide(order.getSide))
    message.setField(new OrderQty(order.getQuantity))
    //orderTableModel.addID(order, id)
    send(message, order.getSessionID)
  }

  def cancel42(order: Order) {
    println("cancelling 4.2 order", order.getSecurityID, order.getSymbol, order.getLimit, order.getQuantity)
    var id: String = order.generateID
    var message: quickfix.fix42.OrderCancelRequest = new quickfix.fix42.OrderCancelRequest(new OrigClOrdID(order.getID), new ClOrdID(id), new Symbol(order.getSymbol), sideToFIXSide(order.getSide), new TransactTime)
    message.set(new OrderQty(order.getQuantity))
    message.getHeader.setField(new SecurityID(order.getSecurityID))
    message.getHeader.setField(new IDSource(order.getIdSource))
    message.getHeader.setField(new SecurityExchange(order.getSecurityExchange))
    message.getHeader.setField(new Currency(order.getCurrency))
    message.getHeader.setField(new SendingTime(new Date))
    message.setField(new OrderQty(order.getQuantity))
    //orderTableModel.addID(order, id)
    send(message, order.getSessionID)
  }

  def onCreate(sessionID: SessionID) {
    println("fromCreate called")
  }

  def fromAdmin(message: Message, sessionID: SessionID) {
    println("fromAdmin called" + message)
    crack(message, sessionID)
  }

  def fromApp(message: Message, sessionID: SessionID) {
    println("fromApp called " + message)
    crack(message, sessionID)
  }

  def getSessionID() = {
    sessionID
  }

  def onLogon(sessionID: SessionID) {
    println("onLogon called")
    loggedIn = true
    this.sessionID = sessionID
    //eventPump.addEvent("logon to fix")
  }

  def onLogout(sessionID: SessionID) = {
    println("onLogout called")
  }

  override def onMessage(message: quickfix.fix42.ExecutionReport, sessionID: SessionID) {
    message.getClOrdID
    message.getOrderID
    val execType = message.getExecType.getValue
/**
    print("exec report ")
    execType match {
      case ExecType.NEW => print("new")
      case '1' => print("partial fill")
      case '2' => print("filled")
      case '4' => print("cancelled")
      case '5' => print("replace")
      case '8' => print("rejected")
      case _ => print(execType)
    }
*/
    eventPump.addEvent(message)

    println
  }

  override def onMessage(message: quickfix.fix42.Heartbeat, sessionID: SessionID) {
    eventPump.addEvent(new Heartbeat)
  }

  override def onMessage(message: quickfix.fix42.Logon, sessionID: SessionID) {
    println("logon recorded")
  }

  override def onMessage(message: quickfix.fix42.NewOrderSingle, sessionID: SessionID) {

    var clOrdID: ClOrdID = new ClOrdID
    message.get(clOrdID)
    var clearingAccount: ClearingAccount = new ClearingAccount
    message.get(clearingAccount)
    println("onMessage " + message)
  }

  override def onMessage(message: quickfix.fix42.OrderCancelReject, sessionID: SessionID) {
    eventPump.addEvent(message)
  }

  override def onMessage(message: quickfix.fix42.OrderCancelRequest, sessionID: SessionID) {
    /**
    var clOrdID: ClOrdID = new ClOrdID
    message.get(clOrdID)
    var clearingAccount: ClearingAccount = new ClearingAccount
   // message.get(clearingAccount)
     */
  }

  private def populateCancelReplace(order: Order, newOrder: Order, message: Message): Message = {
    message.setField(new OrderQty(newOrder.getQuantity))
    message.setField(new Price(newOrder.getLimit.doubleValue))
    return message
  }

  def populateOrder(order: Order, newOrderSingle: Message): Message = {
    var orderType = order.getType.getName
    if (orderType == OrderType.LIMIT.getName) newOrderSingle.setField(new Price(order.getLimit.doubleValue))
    else if (orderType == OrderType.STOP.getName) {
      newOrderSingle.setField(new StopPx(order.getStop.doubleValue))
    }
    else if (orderType == OrderType.STOP_LIMIT.getName) {
      newOrderSingle.setField(new Price(order.getLimit.doubleValue))
      newOrderSingle.setField(new StopPx(order.getStop.doubleValue))
    }
    if (order.getSide == OrderSide.SHORT_SELL.getName || order.getSide == OrderSide.SHORT_SELL_EXEMPT.getName) {
      newOrderSingle.setField(new LocateReqd(false))
    }
    newOrderSingle.setField(tifToFIXTif(order.getTIF))
    return newOrderSingle
  }

  def replace(order: Order, newOrder: Order) {
    var beginString: String = order.getSessionID.getBeginString
    if (beginString == "FIX.4.0") replace40(order, newOrder)
    else if (beginString == "FIX.4.1") replace41(order, newOrder)
    else if (beginString == "FIX.4.2") replace42(order, newOrder)
    return
  }

  def replace40(order: Order, newOrder: Order) {
    var message: quickfix.fix40.OrderCancelReplaceRequest = new quickfix.fix40.OrderCancelReplaceRequest(new OrigClOrdID(order.getID), new ClOrdID(newOrder.getID), new HandlInst('1'), new Symbol(order.getSymbol), sideToFIXSide(order.getSide), new OrderQty(newOrder.getQuantity), typeToFIXType(order.getType))
   // orderTableModel.addID(order, newOrder.getID)
    send(populateCancelReplace(order, newOrder, message), order.getSessionID)
  }

  def replace41(order: Order, newOrder: Order) {
    var message: quickfix.fix41.OrderCancelReplaceRequest = new quickfix.fix41.OrderCancelReplaceRequest(new OrigClOrdID(order.getID), new ClOrdID(newOrder.getID), new HandlInst('1'), new Symbol(order.getSymbol), sideToFIXSide(order.getSide), typeToFIXType(order.getType))
   // orderTableModel.addID(order, newOrder.getID)
    send(populateCancelReplace(order, newOrder, message), order.getSessionID)
  }

  def replace42(order: Order, newOrder: Order) {
    println("replacing 4.2")
    var message: quickfix.fix42.OrderCancelReplaceRequest = new quickfix.fix42.OrderCancelReplaceRequest(new OrigClOrdID(order.getID), new ClOrdID(newOrder.getID), new HandlInst('1'), new Symbol(order.getSymbol), sideToFIXSide(order.getSide), new TransactTime, typeToFIXType(order.getType))
    message.set(new OrderQty(order.getQuantity))
    message.getHeader.setField(new SecurityID(order.getSecurityID))
    message.getHeader.setField(new IDSource(order.getIdSource))
    message.getHeader.setField(new SecurityExchange(order.getSecurityExchange))
    message.getHeader.setField(new Currency(order.getCurrency))
    message.getHeader.setField(new SendingTime(new Date))
   // orderTableModel.addID(order, newOrder.getID)
    send(populateCancelReplace(order, newOrder, message), order.getSessionID)
  }

  def send(order: Order) {

    var beginString: String = sessionID.getBeginString
    if (beginString == FixVersions.BEGINSTRING_FIX40) send40(order)
    else if (beginString == FixVersions.BEGINSTRING_FIX41) send41(order)
    else if (beginString == FixVersions.BEGINSTRING_FIX42) send42(order)
    else if (beginString == FixVersions.BEGINSTRING_FIX43) send43(order)
    else if (beginString == FixVersions.BEGINSTRING_FIX44) send44(order)
    else if (beginString == FixVersions.BEGINSTRING_FIXT11) send50(order)
    return
  }

  private def send(message: Message, sessionID: SessionID) {
    try {
      Session.sendToTarget(message, sessionID)
    }
    catch {
      case e: SessionNotFound => {
        System.out.println(e)
      }
    }
  }

  def send40(order: Order) {
    var newOrderSingle = new quickfix.fix40.NewOrderSingle(new ClOrdID(order.getID), new HandlInst('1'), new Symbol(order.getSymbol), sideToFIXSide(order.getSide), new OrderQty(order.getQuantity), typeToFIXType(order.getType))
    send(populateOrder(order, newOrderSingle), order.getSessionID)
  }

  def send41(order: Order) {
    var newOrderSingle = new quickfix.fix41.NewOrderSingle(new ClOrdID(order.getID), new HandlInst('1'), new Symbol(order.getSymbol), sideToFIXSide(order.getSide), typeToFIXType(order.getType))
    newOrderSingle.set(new OrderQty(order.getQuantity))
    send(populateOrder(order, newOrderSingle), order.getSessionID)
  }

  def send42(order: Order) {
    println("sending 4.2 order")
    var newOrderSingle = new quickfix.fix42.NewOrderSingle(new ClOrdID(order.getID), new HandlInst('1'), new Symbol(order.getSymbol), sideToFIXSide(order.getSide), new TransactTime, typeToFIXType(order.getType))
    newOrderSingle.set(new OrderQty(order.getQuantity))
    newOrderSingle.getHeader.setField(new SecurityID(order.getSecurityID))
    newOrderSingle.getHeader.setField(new IDSource(order.getIdSource))
    newOrderSingle.getHeader.setField(new SecurityExchange(order.getSecurityExchange))
    newOrderSingle.getHeader.setField(new Currency(order.getCurrency))
    newOrderSingle.getHeader.setField(new SendingTime(new Date))
    send(populateOrder(order, newOrderSingle), order.getSessionID)
  }

  def send43(order: Order) {
    var newOrderSingle = new quickfix.fix43.NewOrderSingle(new ClOrdID(order.getID), new HandlInst('1'), sideToFIXSide(order.getSide), new TransactTime, typeToFIXType(order.getType))
    newOrderSingle.set(new OrderQty(order.getQuantity))
    newOrderSingle.set(new Symbol(order.getSymbol))
    send(populateOrder(order, newOrderSingle), order.getSessionID)
  }

  def send44(order: Order) {
    var newOrderSingle = new quickfix.fix44.NewOrderSingle(new ClOrdID(order.getID), sideToFIXSide(order.getSide), new TransactTime, typeToFIXType(order.getType))
    newOrderSingle.set(new OrderQty(order.getQuantity))
    newOrderSingle.set(new Symbol(order.getSymbol))
    newOrderSingle.set(new HandlInst('1'))
    send(populateOrder(order, newOrderSingle), order.getSessionID)
  }

  def send50(order: Order) {
    var newOrderSingle = new quickfix.fix50.NewOrderSingle(new ClOrdID(order.getID), sideToFIXSide(order.getSide), new TransactTime, typeToFIXType(order.getType))
    newOrderSingle.set(new OrderQty(order.getQuantity))
    newOrderSingle.set(new Symbol(order.getSymbol))
    newOrderSingle.set(new HandlInst('1'))
    send(populateOrder(order, newOrderSingle), order.getSessionID)
  }

  def sideToFIXSide(side: OrderSide): Side = {
    sideMap.getFirst(side.getName).asInstanceOf[Side]
  }

  def tifToFIXTif(tif: OrderTIF): TimeInForce = {
    return tifMap.getFirst(tif.getName).asInstanceOf[TimeInForce]
  }

  def toAdmin(message: Message, sessionID: SessionID) {
    message.setField(new ResetSeqNumFlag(true))
    println("toAdmin called")
  }

  def toApp(message: Message, sessionID: SessionID) {
    System.out.println("toApp called" + message)
  }

  def typeToFIXType(orderType: OrderType): OrdType = {
    return typeMap.getFirst(orderType.getName).asInstanceOf[OrdType]
  }
}

class ObservableLogon extends Subject[Any] {
  private val set: HashSet[SessionID] = new HashSet[SessionID]

  def logon(sessionID: SessionID) {
    set.add(sessionID)
    notifyObservers()

  }

  def logoff(sessionID: SessionID) {
    set.remove(sessionID)
    notifyObservers()

  }


}

class LogonEvent {
  def this(sessionID: SessionID, loggedOn: Boolean) {
    this ()
    this.sessionID = sessionID
    this.loggedOn = loggedOn
  }

  def getSessionID: SessionID = {
    return sessionID
  }

  def isLoggedOn: Boolean = {
    return loggedOn
  }

  private var sessionID: SessionID = null
  private var loggedOn: Boolean = false
}