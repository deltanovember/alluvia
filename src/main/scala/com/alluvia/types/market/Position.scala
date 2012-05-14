
package com.alluvia.types.market
import com.alluvialtrading.fix.OrderSide

case class Position(var date: java.util.Date,
                 var security: String,
                 var openPrice: Double,
                 var volume: Double,
                 var value: Double,
                 var direction : OrderSide) {

}
