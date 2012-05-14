package com.alluvia.types

class EventPump {
  var currentEvent: Any = null;

  def addEvent(event: Any) {
    currentEvent = event
  }
}