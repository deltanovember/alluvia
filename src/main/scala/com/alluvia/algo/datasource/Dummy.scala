package com.alluvia.algo.datasource

import com.alluvia.algo.EventAlgo
import com.alluvia.types.market.{DayStart}
import com.alluvial.mds.contract.ReplayConfirmation
import actors.Actor._
import com.alluvia.types.ObservedEventPump

trait Dummy extends EventAlgo {

  // Used to passively push FIX messages
  val eventPump = new ObservedEventPump
  eventPump.addObserver(this)

  actor {
    while (true) {
      eventPump.addEvent(new ReplayConfirmation)
      Thread.sleep(1000)
    }
  }

}