package com.alluvia.types

import com.alluvia.patterns.Subject

  class ObservedEventPump extends EventPump with Subject[Any] {


    override def addEvent(event: Any) {
      super.addEvent(event)
      notifyObservers()
    }
  }