package com.alluvia.sample

import collection.mutable.{HashMap, ListBuffer}
import com.alluvia.algo.{EventAlgo}
import com.alluvia.types.{MagicMap, FixedList}

trait SampleLiveAlgo extends EventAlgo {

  val magic = MagicMap[String](new FixedList[Int](20))
  magic("A").append(1)
  magic("B").append(2)
  magic("B").append(3)
  magic("B").append(4)
  println(magic("B").head)
  println(magic("B").size)
  println(magic("A").size)

  /**
  println("starting")
  val hash = new HashMap[String, FixedList[Int]].withDefaultValue(new FixedList[Int](20))
  hash.getOrElseUpdate()
  hash.::("B").append(2)
  println(hash("B").head)
  println(hash.default("C"))

  def ::(key: String) = {
      hash.getOrElseUpdate(key, hash(key))
  }
   */
}
