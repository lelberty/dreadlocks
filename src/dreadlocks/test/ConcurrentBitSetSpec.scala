package dreadlocks.test

import dreadlocks.core.ConcurrentBitSet
import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.concurrent._
import org.scalatest.FunSpec
import scala.util.Random

class ConcurrentBitSetSpec extends FunSpec {

  describe("ConcurrentBitSet") {
    
    it("should concurrently add items to a set") {
      
      val c = new Conductor 
      import c._

      val range:List[Int] = List.range(0, 64) 
      val toAdd = range.map((_) => { 
        if (Random.nextDouble > .5) 1 
        else 0
      })
      
      val bs = new ConcurrentBitSet(64)
      
      thread("A") {
        for (i <- 0 to 21) if (toAdd(i) == 1) bs += i
      }
      
      thread("B") {
        for (i <- 22 to 42) if (toAdd(i) == 1) bs += i
      }
      
      thread("C") {
        for (i <- 43 to 63) if (toAdd(i) == 1) bs += i
      }
      
      whenFinished{
        range.foreach((i) => {
          if (toAdd(i) == 1) bs.contains(i) should be (true)
          else bs.contains(i) should be (false)
        })
      }
    }
  }
  

}