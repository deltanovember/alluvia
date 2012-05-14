package com.alluvia.types.market

case class OffMarketTrade(var date: java.util.Date, var security: String, var price: Double,
                 var volume: Double, var value: Double,
                      var askBefore: Double, var bidBefore: Double) {
  def midBefore = (askBefore + bidBefore) / 2
}