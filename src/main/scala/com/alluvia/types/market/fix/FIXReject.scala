package com.alluvia.types.market.fix

case class FIXReject(clOrdID: String, val orderID: String, rejectReason: String)