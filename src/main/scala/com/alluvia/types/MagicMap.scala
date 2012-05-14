package com.alluvia.types

/**
 * HashMap with defaults
 */

import scala.collection.mutable.{MapLike, HashMap}

class MagicMap[K, V](defaultValue: => V) extends HashMap[K, V]
with MapLike[K, V, MagicMap[K, V]] {
  override def empty = new MagicMap[K, V](defaultValue)

  override def default(key: K): V = {
    val result = this.defaultValue
    this(key) = result
    result
  }
}

object MagicMap {
  def apply[K] = new Factory[K]

  class Factory[K] {
    def apply[V](defaultValue: => V) = new MagicMap[K, V](defaultValue)
  }

}