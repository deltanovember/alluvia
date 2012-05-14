package com.alluvia.types.market

case class Trade(var date: java.util.Date,
                 var security: String,
                 var price: Double,
                 var volume: Double,
                 var value: Double,
                 var askBefore: Double,
                 var bidBefore: Double,
                  var askVolBefore: Double,
                  var bidVolBefore: Double) {

  def transType = "TRADE";
  def category = "E";
  def spreadBefore = if (bidBefore >= askBefore) Double.NaN else askBefore - bidBefore;
  def midBefore = (askBefore + bidBefore) / 2;
  def spreadBeforePercent = if (bidBefore >= askBefore) Double.NaN else 100 * (askBefore - bidBefore) / midBefore;
  def spreadBeforeBps = if (spreadBeforePercent == Double.NaN) Double.NaN else 100 * spreadBeforePercent;
  def buyOrSell = if (price >= askBefore) 'B' else 'S';
  def tagFields = buyOrSell;

}