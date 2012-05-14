package com.alluvia.types.market


case class SingleOrder(var orderNo: Long,
                        var date: java.util.Date,
                 var security: String,
                 var price: Double,
                 var volume: Double,
                 var value: Double,
                 var askBefore: Double,
                 var bidBefore: Double,
var action: Char,
var bidOrAsk: Char,
var orderType: Int
                        )