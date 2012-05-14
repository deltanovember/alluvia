package sample

import org.scalatest.WordSpec
import collection.mutable.{HashMap, ListBuffer, Stack}
import collection.mutable.{HashMap, Stack}
import java.util.Date

case class MyTrade(security: String, price: Double)

class ExampleSpec extends WordSpec {

  val trade = MyTrade("BHP", 5.0)
  println(trade.security)
  println(trade.price)
}