package com.alluvia.visual.gui

import java.awt.Dimension
import swing._

object SunsetTable extends SimpleSwingApplication {

  var model = Array(List("BHP", 1).toArray)

  lazy val ui = new BoxPanel(Orientation.Vertical) {
    val table = new Table(model, Array("Security", "Price")) {
      preferredViewportSize = new Dimension(1200, 600)
    }
    contents += new ScrollPane(table)
  }

  def top = new MainFrame {
    contents = ui
  }

}

//  var model = Array(List("BHP", "45", "46", 1.5, 45.5, 2.3).toArray,
//    List("RIO", "45", "46", 1.5, 45.5, 2.3).toArray,
//    List("WBC", "45", "46", 1.5, 45.5, 2.3).toArray,
//    List("CBA", "45", "46", 1.5, 45.5, 2.3).toArray,
//    List("WES", "45", "46", 1.5, 45.5, 2.3).toArray)
//
//  lazy val ui = new BoxPanel(Orientation.Vertical) {
//    val table = new Table(model, Array("Security", "Last Bid", "Last Ask", "Last Spread", "Close Price", "Delta")) {
//      preferredViewportSize = new Dimension(1200, 600)
//    }
//    contents += new ScrollPane(table)
//  }
//
//
//  def top = new MainFrame {
//    title = "Auction Changes"
//    contents = ui
//  }

/**
Thread.sleep(15000)
  model = Array(List("PRY", "45", "46", 1.5, 45.5, 2.3).toArray,
                    List("RIO", "45", "46", 1.5, 45.5, 2.3).toArray,
                    List("WBC", "45", "46", 1.5, 45.5, 2.3).toArray,
                    List("CBA", "45", "46", 1.5, 45.5, 2.3).toArray,
                    List("WES", "45", "46", 1.5, 45.5, 2.3).toArray)*/
