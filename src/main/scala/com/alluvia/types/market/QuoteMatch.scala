package com.alluvia.types.market

case class QuoteMatch(var date: java.util.Date,
                      var security: String,
                      var price: Double,
                      var volume: Double,
                      var value: Double,
                      var askBefore: Double,
                      var bidBefore: Double,
                       var surplus: Double) {
  def midBefore = (askBefore + bidBefore) / 2

  def spreadBefore = askBefore - bidBefore

  def spreadBeforePercent = 100 * (askBefore - bidBefore) / midBefore
}