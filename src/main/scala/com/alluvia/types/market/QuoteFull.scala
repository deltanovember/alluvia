package com.alluvia.types.market

case class QuoteFull(var date: java.util.Date,
                     var security: String,
                     var openPrice: Double,
                     var maxPrice: Double,
                     var minPrice: Double,
                     var closePrice: Double,
                     var securityStatus: Char) {
  
  val QUOTE_FULL = "QUOTE_FULL"
  def transType = QUOTE_FULL;
  def category = "E";
  
}
