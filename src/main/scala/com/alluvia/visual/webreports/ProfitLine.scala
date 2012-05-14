package com.alluvia.visual.webreports

case class ProfitLine(var id: Int, transType: String, summary: String, date: String, ref: String, desc: String, period: String,
                      opening: String, currency: String, size: String, closing: String, var amount: Double) {

  override def toString = transType + "," + summary + "," + date + "," + ref + "," + desc + "," + period + "," + opening + "," + currency + "," + size + "," + closing + "," + amount
}