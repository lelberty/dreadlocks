package dreadlocks.test
import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.concurrent._
import org.scalatest.FunSpec


class DerpSpec extends FunSpec {

  describe("A @volatile var") {
    
    it("should never be null after assignment") {
      
      @volatile var objA:String = null
      @volatile var objB:String = null
      @volatile var objC:String = null
              
      for (i <- 1 to 1000) {
        
        val c = new Conductor
        import c._
        
        objA = null
        objB = null
        objC = null
        
        thread("A") {
          objA = "objA"
          waitForBeat(1)
          objB should not be (null)        
        }
        
        thread("B") {
          objB = "objB"
          waitForBeat(1)
          objC should not be (null)
        }
        
        thread("C") {
          objC = "objC"
          waitForBeat(1)
          objC should not be (null)
        }
        
        whenFinished {
          beat should be (1)
        }
        
      }
    }

  }
}
