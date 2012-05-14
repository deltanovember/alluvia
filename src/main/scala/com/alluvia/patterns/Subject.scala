package com.alluvia.patterns

trait Subject[S] {
    this: S =>
    private var observers: List[Observer[S]] = Nil
    def addObserver(observer: Observer[S]) = observers = observer :: observers

    def notifyObservers() = observers.foreach(_.receiveUpdate(this))
}