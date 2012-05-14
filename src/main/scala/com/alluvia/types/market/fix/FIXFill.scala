package com.alluvia.types.market.fix

case class FIXFill(orderID: String, clOrdID: String, var security: String, var price: Double,
                 var volume: Double)