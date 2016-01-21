package test.com.blueskiron.bilby.io.core

import org.scalatest.FlatSpec
import com.blueskiron.bilby.io.core.util.MutableLRU
import org.scalatest._

class MutableLRUSpec extends FlatSpec with Matchers {

  case class Key(x: Int)
  case class Value(x: String)

  private val maxSize = 10
  
  private val cache: MutableLRU[Key, Value] = MutableLRU[Key, Value](maxSize);

  "MutableLRU" should s"store maximum number of elements: $maxSize" in {

    for (i <- 1 to (maxSize * 2)) {
      cache + (
        (Key(i),
          Value(java.util.UUID.randomUUID().toString())))
    }
    println("final cahe: " + cache)
    cache.size shouldBe maxSize
  }
  
}