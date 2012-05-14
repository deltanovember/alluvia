package com.alluvia.patterns

trait Observer[S] {
    def receiveUpdate(subject: S);
}