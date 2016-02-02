package com.blueskiron.bilby.io.core.util

import scala.collection.mutable.Map
import scala.collection.mutable.LongMap
import scala.collection.mutable.SortedSet
import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.Future

/**
 * A simple implementation of a mutable LRU cache.
 *
 * Since Actors share nothing this mutable implementation is acceptable.
 *
 * @author juri
 */
object MutableLRU {

  /**
   * Build an immutable LRU key/value store that cannot grow larger than `maxSize`
   */
  def apply[K, V](maxSize: Int) = {
    new MutableLRU(maxSize, Map[K, (Long, V)](), LongMap[(K, Long)]())
  }
}

/**
 * An mutable key/value store that evicts the least recently accessed elements
 * to stay constrained in a maximum size bound.
 */
class MutableLRU[K, V] private (maxSize: Int, map: Map[K, (Long, V)], ord: LongMap[(K, Long)]) {

  val insertIdx = new AtomicLong()
  /**
   * the number of entries in the cache
   */
  def size: Int = ord.size

  /**
   * the `Set` of all keys in the LRU
   * @note accessing this set does not update the element LRU ordering
   */
  def keySet: Set[K] = Set() ++ map.keySet

  private def lessThan(a: (K, Long), b: (K, Long)) = a._2 < b._2

  private def moreThan(a: (K, Long), b: (K, Long)) = a._2 > b._2

  private def firstKey = ord.values.toSeq.sortWith(lessThan).head

  private def lastKey = ord.values.toSeq.sortWith(moreThan).head

  def leastRecentEntry: Option[K] = if (ord.isEmpty) None else Some(firstKey._1)

  def mostRecentEntry: Option[K] = if (ord.isEmpty) None else Some(lastKey._1)

  private def idx: Long = if (ord.isEmpty) 0 else firstKey._2

  /**
   * add operation
   * @return an optional element representing the evicted entry (if the given lru is at the maximum size)
   * or None if the lru is not at capacity yet.
   */
  def +(kv: (K, V)): Option[(K, V)] = {
    val (key, value) = kv
    val index = insertIdx.incrementAndGet()
    map.put(key, (index, value))
    ord.put(index, (key, 0L))
    // Do we need to remove an old key?
    if (size > maxSize) {
      val lru = firstKey._1
      val evicted = map.get(lru)
      evicted.flatMap(x => {
        destroy(lru, x._1)
        Some((lru, x._2))
      })
    } else {
      None
    }
  }

  /**
   * If the key is present in the cache, return Some(value)
   * Increment the counter for the key.
   * Else, return None.
   */
  def get(key: K): Option[V] = {
    map.get(key).map(e => {
      incrementAccess(e._1)
      e._2
    })
  }
  
  /**
   * Check if the given key is contained in this cache.
   * @param key
   * @return
   */
  def contains(key: K): Boolean = map.contains(key)

  /**
   * If the key is present in the cache, returns the pair of
   * Some(value) and the cache with the key removed.
   * Else, returns None.
   */
  def -(k: K): Option[V] = {
    val opt = map.get(k)
    opt.map(x => {
      destroy(k, x._1)
      x._2
    })
  }

  private def incrementAccess(ordKey: Long) {
    ord.get(ordKey).map(e => ord.put(ordKey, (e._1, e._2 + 1)))
  }

  private def destroy(mapKey: K, ordKey: Long) {
    ord.get(ordKey).map { kRef =>
      map.remove(kRef._1)
      ord.remove(ordKey)
    }
  }

  def toMap: scala.collection.immutable.Map[K, V] = {
    //return a copy of the underlying map
    scala.collection.immutable.Map[K, V]() ++ this.map.map(x => x._1 -> x._2._2)
  }

  override def toString = { s"MutableLRU(size=$size, lru=$leastRecentEntry, mru=$mostRecentEntry)" }
}