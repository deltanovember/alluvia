package com.alluvia.types.market.fix

case class FIXPartialFill(orderID: String, clOrdID: String, var security: String, var price: Double,
                 var volume: Double)