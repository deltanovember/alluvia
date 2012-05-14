package com.alluvia.types.market

case class Quote(var date: java.util.Date,
                 var security: String,
                 var price: Double,
                 var bid: Double,
                 var ask: Double,
                 var volume: Double,
                 var value: Double,
                 var bidVol: Double,
                 var askVol: Double,
                 var bidBefore: Double,
                 var askBefore: Double,
                 var bidVolBefore: Double,
                 var askVolBefore: Double,
                 var bidOrAsk: Char) {

  val QUOTE = "QUOTE"
  def transType = QUOTE;
  def category = "E";
  def spread = if (bid >= ask) Double.NaN else ask - bid;
  def mid = (ask + bid) / 2;
  def spreadPercent = if (bid >= ask) Double.NaN else 100 * (ask - bid) / mid;
  def spreadBps = if (spreadPercent == Double.NaN) Double.NaN else 100 * spreadPercent;
  def tagFields = bidOrAsk;
  
}
