package dreadlocks.test

import dreadlocks.core._

import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.concurrent._
import org.scalatest.FunSpec
import org.scalatest.BeforeAndAfter

import scala.collection.immutable.HashMap

import java.util.concurrent.locks.ReentrantLock

class DreadLockSpec extends FunSpec with BeforeAndAfter {
  
  val NUM_RUNS = 1000
  
  val R1 = "R1"
  val R2 = "R2"
  val R3 = "R3"
  val success = DreadLockStatus.Success
  val deadlock = DreadLockStatus.Deadlock
  
  private def makeWaitForFun(numThreads: Int, c: Conductor) : (Int => Unit) = {
    
    var arrived : Int = 0
    var expectedBeat : Int = 1
    val beatMap : HashMap[Int, Int] = new HashMap[Int, Int]()
    
    val l : ReentrantLock = new ReentrantLock()
    
    (beat : Int) => {

      c.waitForBeat(beat)

      l.synchronized {
        
        if (c.beat > expectedBeat) throw new Exception(
          "Beat advanced too soon: expected %d, got %d".format(expectedBeat, c.beat))
        
        arrived = arrived + 1
        
        if (arrived == numThreads) {
          arrived = 0
          expectedBeat = expectedBeat + 1
        }
      }
    }
  }  
  
  describe("DreadLock") {
    
    it ("should let two threads lock separate resources with no competition") {

      for (i <- 1 to NUM_RUNS) {
        
        Ticket.reset()
        val c = new Conductor()
        import c._
        val dl = new DreadLock()
        
        thread("A") {
          dl.lock(R1) should be (success)
          dl.unlock(R1)
        }
        
        thread("B") {
          dl.lock(R2) should be (success)
          dl.unlock(R2)
        }
        
        whenFinished {}

      }
      
    }
    
    it ("should successfully manage several resources with no deadlock") {
      
      for (i <- 1 to NUM_RUNS) {

        Ticket.reset() 
        val c = new Conductor()
        val dl = new DreadLock()
        import c._

        thread("A") { 
          dl.lock(R1) should be (success)
          dl.lock(R2) should be (success)
          dl.unlock(R2) 
          dl.unlock(R1)
        }
        
        thread("B") {
          dl.lock(R2) should be (success)
          dl.lock(R3) should be (success)
          dl.unlock(R3)
          dl.unlock(R2)
        }
        
        thread("C") {
          dl.lock(R3) should be (success)
          dl.unlock(R3)
        }
        
        whenFinished {}
      }

    }
    

 	  it ("should correctly detect a 2-thread deadlock") {
      
      for (i <- 1 to NUM_RUNS) {
        
        // println("\n -- RUN %d -- \n".format(i))
        
        Ticket.reset()
    	  
        val c = new Conductor()
      	import c._
      	val dl = new DreadLock()
        
      	thread("A") {
          dl.lock(R1) should be (success)
          // println("A acquired R1")
          waitForBeat(1)
          // println("A passed beat 1")
          dl.lock(R2) should be (deadlock)
        }
        
        thread("B") {
          dl.lock(R2) should be (success)
          // println("B acquired R2")
          waitForBeat(1)
          // println("B passed beat 1")
          dl.lock(R1) should be (deadlock)
        }
        
        whenFinished {}
      }
      
    }
    
  }
  
  it ("should correctly detect a 3-thread deadlock") {
    
    for (i <- 1 to NUM_RUNS) {
      
      // println("\n -- RUN %d -- ".format(i))

      Ticket.reset()

      val c = new Conductor()
      import c._
      val dl = new DreadLock()

      thread("A") {
        dl.lock(R1) should be (success)
        // println("A acquired R1");
        waitForBeat(1)
        dl.lock(R2) should be (deadlock)
      }

      thread("B") {
        dl.lock(R2) should be (success)
        // println("B acquired R2")
        waitForBeat(1)
        dl.lock(R3) should be (deadlock)
      }

      thread("C") {
        dl.lock(R3) should be (success)
        // println("C acquired R3")
        waitForBeat(1)
        dl.lock(R1) should be (deadlock)
      }

      whenFinished {}

    }

  }
  
}
