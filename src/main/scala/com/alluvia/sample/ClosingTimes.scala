package com.alluvia.sample

import com.alluvia.algo.EventAlgo
import java.util.Date
import com.alluvia.types.MagicMap
import com.alluvia.types.market.{DayEnd, Trade}

/**
 * @author Don Nguyen
 * @author Matthew Clifton
 */

trait ClosingTimes
  extends EventAlgo {

  var closingDate = new Date
  val trackingSecurity = "BHP"

  override def onTrade(t: Trade) {
   if (t.security == trackingSecurity) closingDate = t.date
  }

  override def onDayEnd(d: DayEnd) {
    //val seconds = date.getMinutes * 60 + date.getSeconds'
  }
}