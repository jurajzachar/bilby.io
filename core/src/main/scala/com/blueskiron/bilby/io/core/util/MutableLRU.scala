package com.blueskiron.bilby.io.core.util

import scala.collection.mutable.Map
import scala.collection.mutable.SortedSet

/**
 * A simple implementation of a mutable LRU cache.
 *
 * Since Actors share nothing this mutable implementation is fine.
 *
 * @author juri
 */
object MutableLRU {

  /**
   * Build an immutable LRU key/value store that cannot grow larger than `maxSize`
   */
  def apply[K, V](maxSize: Int): MutableLRU[K, V] = {

    implicit val keyOrd: Ordering[(Long, K)] = new Ordering[(Long, K)] {
      def compare(a: (Long, K), b: (Long, K)) = a._1 compare b._1
    }

    new MutableLRU(maxSize, Map[K, (Long, V)](), SortedSet[(Long, K)]())
  }
}

/**
 * An mutable key/value store that evicts the least recently accessed elements
 * to stay constrained in a maximum size bound.
 */
class MutableLRU[K, V] private (maxSize: Int, map: Map[K, (Long, V)], ord: SortedSet[(Long, K)])(implicit cmp: Ordering[(Long, K)]) {

  /**
   * the number of entries in the cache
   */
  def size: Int = ord.size

  /**
   * the `Set` of all keys in the LRU
   * @note accessing this set does not update the element LRU ordering
   */
  def keySet: Set[K] = Set() ++ map.keySet

  def idx: Long = if (ord.isEmpty) 0 else ord.firstKey._1

  /**
   * add operation
   * @return an optional element representing the evicted entry (if the given lru is at the maximum size)
   * or None if the lru is not at capacity yet.
   */
  def +(kv: (K, V)): Option[(K, V)] = {
    val (key, value) = kv
    getWithIdx(key) match {
      case Some((k, v)) => {
        val newIdx = k
        map.put(key, (newIdx, value))
      }
      case None => {
        map.put(key, (idx, value))
      }
    }
    // Do we need to remove an old key?
    if (size > maxSize) {
      println("evicting from: " + ord)
      val lru = ord.firstKey._2
      val evicted = map.get(lru)
      evicted.flatMap(x => {
        destroy(lru)
        Some((lru, x._2))
      })
    } else {
      None
    }
  }

  /**
   * If the key is present in the cache, return the pair of Some(value)
   * Increment the counter for the key.
   * Else, return None.
   */
  private def getWithIdx(key: K): Option[(Long, V)] = {
    val opt = map.get(key)
    opt.map(x => {
      val eKey = (x._1, key)
      val eVal = x._2
      incrementAccess(eKey, eVal)
      x
    })
  }

  def get(key: K): Option[V] = {
    getWithIdx(key).map(_._2)
  }

  /**
   * If the key is present in the cache, returns the pair of
   * Some(value) and the cache with the key removed.
   * Else, returns None.
   */
  def -(k: K): Option[V] = {
    val opt = map.get(k)
    opt.map(x => {
      destroy(k)
      x._2
    })
  }

  private def incrementAccess(eKey: (Long, K), eVal: V) {
    ord.find(x => x._2 == eKey).map {
      entry =>
        {
          val updated = (entry._1 + 1, entry._2)
          ord ++ ((ord diff Set(entry)) ++ Set(updated)) //handle ord
          map.put(updated._2, (updated._1, eVal)) //handle map
        }
    }
  }

  private def destroy(key: K) {
    ord.find(x => x._2 == key).map {
      entry =>
        {
          ord ++ (ord diff Set(entry)) //handle ord
          map.remove(key) //handle map
        }
    }
  }

  override def toString = { "MutableLRU(" + map.toList.mkString(",\n") + ")" }
}