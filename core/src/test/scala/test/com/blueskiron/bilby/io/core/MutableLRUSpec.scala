package test.com.blueskiron.bilby.io.core

import org.scalatest.FlatSpec
import com.blueskiron.bilby.io.core.util.MutableLRU
import org.scalatest._

class MutableLRUSpec extends FlatSpec with Matchers {

  case class Key(x: Int)
  case class Value(x: String)
  implicit val keyOrd: Ordering[Key] = new Ordering[Key] {
    def compare(a: Key, b: Key) = a.x compare b.x
  }

  private val maxSize = 10 * 10 * 10 * 10 *10 * 10 

  private val cache: MutableLRU[Key, Value] = MutableLRU[Key, Value](maxSize);

  
  "MutableLRU" should s"store only up to maximum number of elements: $maxSize" in {

    //intentionally loop one iteration more to check the upper bound of cache.
    for (i <- 1 to maxSize + 10) {
      cache + (
        (Key(i),
          Value(java.util.UUID.randomUUID().toString())))
    }

    println("final cahe: " + cache)
    cache.size shouldBe maxSize
  }

  "MutableLRU" should s"update access times" in {

    val chosenKey = Key(maxSize-1)
    //intentionally loop twice as long to check the delete on empty does not throw and is idempotent.
    for (i <- 1 to maxSize) {
      //even nrs get more hits 
      if (i % 2 == 0) {
        //access Key(1)
        cache.get(chosenKey)
      } else {
        //access Key(i) once
        cache.get(Key(i))
      }
    }
    println("final cahe: " + cache)
    cache.mostRecentEntry shouldBe Some(chosenKey)
  }

    "MutableLRU" should s"delete elements: $maxSize" in {
  
      //intentionally loop twice as long to check the delete on empty does not throw and is idempotent.
      for (i <- 1 to (maxSize * 2)) {
        cache - (Key(i))
      }
      println("final cahe: " + cache)
      cache.size shouldBe 0
    }
}