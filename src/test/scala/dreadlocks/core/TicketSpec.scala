package dreadlocks.test

import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.concurrent._
import org.scalatest.FunSpec
import dreadlocks.core.Ticket

class TicketSpec extends FunSpec {
  
  private val NUM_RUNS = 1000
	
  describe("A Ticket") {
    
    //    it ("should correctly disperse Tickets to one thread") {
    //      
    //      var t:Ticket = Ticket.takeTicket
    //      t.getHoldCount should be (1)
    //      t.isFree should be (false)
    //    
    //      Ticket.takeTicket should be theSameInstanceAs t
    //      t.isFree should be (false)
    //      t.getHoldCount should be (2)
    //
    //      Ticket.takeTicket should be theSameInstanceAs t
    //      t.isFree should be (false)
    //      t.getHoldCount should be (3)
    //      
    //      Ticket.returnTicket should be (true)
    //      t.isFree should be (false)
    //      t.getHoldCount should be (2)
    //      
    //      Ticket.returnTicket should be (true)
    //      t.isFree should be (false)
    //      t.getHoldCount should be (1)
    //      
    //      Ticket.returnTicket should be (false)
    //      t.isFree should be (true)
    //      t.getHoldCount should be (0)
    //      
    //      intercept[DreadLockException] {
    //        Ticket.returnTicket
    //      }
    //    }
    //    
    //    it ("should correctly disperse Tickets to multiple threads") {
    //     
    //      val c = new Conductor 
    //      import c._
    //     
    //      thread("A") {
    //        var t1:Ticket  = Ticket.takeTicket
    //        t1.getHoldCount should be (1)
    //        t1.isFree should be (false)
    //        
    //        Ticket.takeTicket should be theSameInstanceAs t1
    //        t1.isFree should be (false)
    //        t1.getHoldCount should be (2)
    //
    //        Ticket.takeTicket should be theSameInstanceAs t1
    //        t1.isFree should be (false)
    //        t1.getHoldCount should be (3)
    //      
    //        Ticket.returnTicket should be (true)
    //        t1.isFree should be (false)
    //        t1.getHoldCount should be (2)
    //      
    //        Ticket.returnTicket should be (true)
    //        t1.isFree should be (false)
    //        t1.getHoldCount should be (1)
    //      
    //        Ticket.returnTicket should be (false)
    //        t1.isFree should be (true)
    //        t1.getHoldCount should be (0)
    //      }
    //      
    //      thread("B") {
    //        var t2:Ticket  = Ticket.takeTicket
    //        t2.getHoldCount should be (1)
    //        t2.isFree should be (false)
    //        
    //        Ticket.takeTicket should be theSameInstanceAs t2
    //        t2.isFree should be (false)
    //        t2.getHoldCount should be (2)
    //
    //        Ticket.takeTicket should be theSameInstanceAs t2
    //        t2.isFree should be (false)
    //        t2.getHoldCount should be (3)
    //      
    //        Ticket.returnTicket should be (true)
    //        t2.isFree should be (false)
    //        t2.getHoldCount should be (2)
    //      
    //        Ticket.returnTicket should be (true)
    //        t2.isFree should be (false)
    //        t2.getHoldCount should be (1)
    //      
    //        Ticket.returnTicket should be (false)
    //        t2.isFree should be (true)
    //        t2.getHoldCount should be (0)
    //      }
    //      
    //      thread("C") {
    //        var t3:Ticket  = Ticket.takeTicket
    //        t3.getHoldCount should be (1)
    //        t3.isFree should be (false)
    //        
    //        Ticket.takeTicket should be theSameInstanceAs t3
    //        t3.isFree should be (false)
    //        t3.getHoldCount should be (2)
    //
    //        Ticket.takeTicket should be theSameInstanceAs t3
    //        t3.isFree should be (false)
    //        t3.getHoldCount should be (3)
    //      
    //        Ticket.returnTicket should be (true)
    //        t3.isFree should be (false)
    //        t3.getHoldCount should be (2)
    //      
    //        Ticket.returnTicket should be (true)
    //        t3.isFree should be (false)
    //        t3.getHoldCount should be (1)
    //      
    //        Ticket.returnTicket should be (false)
    //        t3.isFree should be (true)
    //        t3.getHoldCount should be (0)
    //      }
    //    }
    //  }
    //  
    //  it ("should handle many threads rapidly trying to acquire and then return a single ticket") {
    //    val c = new Conductor 
    //    import c._
    //     
    //    thread("A") {
    //      var t_a:Ticket  = Ticket.takeTicket
    //      
    //      for (i <- 1 to 1000) {
    //        t_a.getHoldCount() should be (1)
    //        t_a.isFree should be (false)
    //        Ticket.returnTicket
    //        t_a.getHoldCount should be (0)
    //        t_a.isFree should be (true)
    //        t_a = Ticket.takeTicket
    //      }
    //      
    //      Ticket.returnTicket
    //      t_a.getHoldCount should be (0)
    //      t_a.isFree should be (true)
    //
    //
    //    }
    //    
    //    thread("B") {
    //      var t_b:Ticket  = Ticket.takeTicket
    //      
    //      for (i <- 1 to 1000) {
    //        t_b.getHoldCount() should be (1)
    //        t_b.isFree should be (false)
    //        Ticket.returnTicket
    //        t_b.getHoldCount should be (0)
    //        t_b.isFree should be (true)
    //        t_b = Ticket.takeTicket
    //      }
    //      
    //      Ticket.returnTicket
    //      t_b.getHoldCount should be (0)
    //      t_b.isFree should be (true)
    //    }
    //    
    //    thread("C") {
    //      var t_c:Ticket  = Ticket.takeTicket
    //      
    //      for (i <- 1 to 1000) {
    //        t_c.getHoldCount() should be (1)
    //        t_c.isFree should be (false)
    //        Ticket.returnTicket
    //        t_c.getHoldCount should be (0)
    //        t_c.isFree should be (true)
    //        t_c = Ticket.takeTicket
    //      }
    //      
    //      Ticket.returnTicket
    //      t_c.getHoldCount should be (0)
    //      t_c.isFree should be (true)
    //    }
    //
    //  }
    //    
//    it ("should detect a simple 2-thread cycle") {
//
//      for (i <- 1 to NUM_RUNS) {
//        
//        println("\n --- RUN %d --- \n\n".format(i))
//        
//        val c = new Conductor 
//        import c._
//        
//        Ticket.reset
//        
//        var tA:Ticket = null
//        var tB:Ticket = null
//        
//        var deadlockA = true
//        var deadlockB = true
//        
//        thread("A") {
//          tA = Ticket.takeTicket
//          waitForBeat(1)
//          waitForBeat(2)
//          println("A passed beat 2")
//          tA.union(tB)
//          tA.cycleCheck()
//          waitForBeat(3) // mimic quiescence
//          waitForBeat(4)
//          println("A passed beat 4")
//          deadlockA = tA.cycleCheck()
//        }
//        
//        thread("B") {
//          tB = Ticket.takeTicket
//          waitForBeat(1)
//          waitForBeat(2)
//          println("A passed beat 2")
//          tB.union(tA)
//          tB.cycleCheck()
//          waitForBeat(3) // mimic quiescenc
//          waitForBeat(4)
//          println("A passed beat 4")
//          deadlockB = tB.cycleCheck()
//        }
//
//        whenFinished{
//          println("deadlockA = %b, deadlockB = %b".format(deadlockA, deadlockB))
//          (deadlockA && deadlockB) should be (true) 
//        }
//      }
//
//    }
    
    it ("should detect a simple 3-thread cycle") {

      @volatile var tA:Ticket = null
      @volatile var tB:Ticket = null
      @volatile var tC:Ticket = null
      
      var deadlockA = true
      var deadlockB = true
      var deadlockC = true
      
      for (i <- 1 to NUM_RUNS) {
        
        println("\n --- RUN %d --- \n".format(i))
        
        Ticket.reset
        
        val c = new Conductor
        import c.thread
        import c.whenFinished
        import c.waitForBeat
        
        tA = null
        tB = null
        tC = null
        
        deadlockA = true
        deadlockB = true
        deadlockC = true

        thread("A") {
          tA = Ticket.takeTicket
          println("A's ticket: " + tA.getTicketNum())
          waitForBeat(1)
          waitForBeat(2)
          println("A passed beat 2")
          tA.union(tB)
          tA.cycleCheck()
          waitForBeat(3)
          waitForBeat(4)
          println("A passed beat 4")
          deadlockA = tA.cycleCheck()
        }
        
        thread("B") {
          tB = Ticket.takeTicket
          println("B's ticket: " + tB.getTicketNum())
          waitForBeat(1)
          waitForBeat(2)
          println("B passed beat 2")
          tC should not be (null)
          tB.union(tC)
          tB.cycleCheck()
          waitForBeat(3)
          waitForBeat(4)
          println("B passed beat 4")
          deadlockB = tB.cycleCheck()
        }
        
        thread("C") {
          tC = Ticket.takeTicket
          println("C's ticket: " + tC.getTicketNum())
          waitForBeat(1)
          waitForBeat(2)
          println("C passed beat 2")
          tA should not be (null)
          tC.union(tA)
          tC.cycleCheck()
          waitForBeat(3)
          waitForBeat(4)
          println("C passed beat 4")
          deadlockC = tC.cycleCheck()
        }
        
        whenFinished{
          println("deadlockA: %b, deadlockB: %b, deadlockC: %b"
                  .format(deadlockA, deadlockB, deadlockC))
          (deadlockA && deadlockB && deadlockC) should be (true) 
        }
        
      } 
    }
    
  }  
  
}


	