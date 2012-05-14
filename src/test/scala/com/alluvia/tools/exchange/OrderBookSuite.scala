package com.alluvia.tools.exchange

import org.scalatest.WordSpec
import com.alluvia.markets.LSE
import com.alluvia.types.market.SingleOrder._
import java.util.Date
import com.alluvia.types.market.SingleOrder

class OrderBookSuite extends WordSpec {

  "An Order Book" should {
    "produce surplus" in {
      val date = new Date
      val security = "BHP"
      val askBefore = 0
      val bidBefore = 0
      val value = 0.0
      val o1 = SingleOrder(0L, date, security, 0.425, 10000, value, askBefore, bidBefore, 'A', 'B', 0)
      val o2 = SingleOrder(0L, date, security, 0.43, 10000, value, askBefore, bidBefore, 'A', 'A', 0)
      val o3 = SingleOrder(0L, date, security, 0.42, 10000, value, askBefore, bidBefore, 'A', 'B', 0)
      val o4 = SingleOrder(0L, date, security, 0.425, 3000, value, askBefore, bidBefore, 'A', 'A', 0)
      val book = new OrderBook
      book.addOrder(o1)
      book.addOrder(o2)
      book.addOrder(o3)
      book.addOrder(o4)
      val qm = book.getQuoteMatch
      //book.displayBook()
      assert(qm.price == 0.425)
      assert(qm.volume == 3000.0)
      assert(qm.surplus == 7000.0)
      //assert(lse.getMarketName === "LSE")
      //"min tick size" is (pending)
    }
    "match complex uncrossing" in {
      val date = new Date
      val security = "BHP"
      val askBefore = 0
      val bidBefore = 0
      val value = 0.0
      val o1 = SingleOrder(0L, date, security, 40.01, 10000, value, askBefore, bidBefore, 'A', 'B', 0)
      val o2 = SingleOrder(0L, date, security, 45.0, 10, value, askBefore, bidBefore, 'A', 'B', 0)
      val o3 = SingleOrder(0L, date, security, 40.0, 5500, value, askBefore, bidBefore, 'A', 'B', 0)
      val o4 = SingleOrder(0L, date, security, 37.0, 100000, value, askBefore, bidBefore, 'A', 'B', 0)
      val o5 = SingleOrder(0L, date, security, 39.99, 7500, value, askBefore, bidBefore, 'A', 'B', 0)


      val o6 = SingleOrder(0L, date, security, 40, 9000, value, askBefore, bidBefore, 'A', 'A', 0)
      val o7 = SingleOrder(0L, date, security, 38.5, 1000, value, askBefore, bidBefore, 'A', 'A', 0)
      val o8 = SingleOrder(0L, date, security, 40.01, 3000, value, askBefore, bidBefore, 'A', 'A', 0)
      val o9 = SingleOrder(0L, date, security, 39.98, 4500, value, askBefore, bidBefore, 'A', 'A', 0)
      val o10 = SingleOrder(0L, date, security, 44.9, 6000, value, askBefore, bidBefore, 'A', 'A', 0)

      val book = new OrderBook
      book.addOrder(o1)
      book.addOrder(o2)
      book.addOrder(o3)
      book.addOrder(o4)
      book.addOrder(o5)
      book.addOrder(o6)
      book.addOrder(o7)
      book.addOrder(o8)
      book.addOrder(o9)
      book.addOrder(o10)

      val qm = book.getQuoteMatch
      book.displayBook()
      assert(qm.price == 40.0)
      assert(qm.volume == 14500.00)
      assert(qm.surplus == 1010.0)
      val startTime = System.currentTimeMillis()
      for (i <- 0 to 1000000) book.getQuoteMatch
      println(System.currentTimeMillis() - startTime + "ms")
    }
    "produce negative surplus" in {
      val date = new Date
      val security = "BHP"
      val askBefore = 0
      val bidBefore = 0
      val value = 0.0
      val o1 = SingleOrder(0L, date, security, 40.01, 10000, value, askBefore, bidBefore, 'A', 'B', 0)
      val o2 = SingleOrder(0L, date, security, 40.01, 15000, value, askBefore, bidBefore, 'A', 'A', 0)

      val book = new OrderBook
      book.addOrder(o1)
      book.addOrder(o2)

      val qm = book.getQuoteMatch
      assert(qm.price == 40.01)
      assert(qm.volume == 10000.00)
      assert(qm.surplus == -5000.000)
    }
    "handle layers" in {
      val date = new Date
      val security = "BHP"
      val askBefore = 0
      val bidBefore = 0
      val value = 0.0
      val o1 = SingleOrder(0L, date, security, 40.01, 10000, value, askBefore, bidBefore, 'A', 'B', 0)
      val o2 = SingleOrder(0L, date, security, 40.01, 15000, value, askBefore, bidBefore, 'A', 'A', 0)
      val o3 = SingleOrder(0L, date, security, 40.01, 10000, value, askBefore, bidBefore, 'A', 'B', 0)

      val book = new OrderBook
      book.addOrder(o1)
      book.addOrder(o2)
      book.addOrder(o3)

      val qm = book.getQuoteMatch
      assert(qm.price == 40.01)
      assert(qm.volume == 15000.00)
      assert(qm.surplus == 5000.000)
    }
    "handle no uncrossings" in {
      val date = new Date
      val security = "BHP"
      val askBefore = 0
      val bidBefore = 0
      val value = 0.0
      val o1 = SingleOrder(0L, date, security, 40.01, 10000, value, askBefore, bidBefore, 'A', 'B', 0)
      val o2 = SingleOrder(0L, date, security, 40.02, 15000, value, askBefore, bidBefore, 'A', 'A', 0)

      val book = new OrderBook
      book.addOrder(o1)
      book.addOrder(o2)

      val qm = book.getQuoteMatch
      assert(qm.volume == 0)
    }
    "produce indicatives" in {

    }

  }
}