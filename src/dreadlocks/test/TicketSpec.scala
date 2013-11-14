package dreadlocks.test

import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.concurrent._
import org.scalatest.FunSpec
import dreadlocks.core.Ticket

class TicketSpec extends FunSpec {
	
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
//      val c = new Conductor 
//      import c._
//    
//      Ticket.reset
//      
//      var tA:Ticket = null
//      var tB:Ticket = null
//      
//      var successA = true
//      var successB = true
//            
//      thread("A") {
//        tA = Ticket.takeTicket
//        waitForBeat(1)
//        successA = tA.union(tB)
//      }
//      
//      thread("B") {
//        tB = Ticket.takeTicket
//        waitForBeat(1)
//        successB = tB.union(tA)
//      }
//
//      whenFinished{
//        (!successA || !successB) should be (true) 
//      }
//    }

  it ("should detect a simple 3-thread cycle") {

    @volatile var tA:Ticket = null
    @volatile var tB:Ticket = null
    @volatile var tC:Ticket = null
    
    var successA = true
    var successB = true
    var successC = true
    
    for (i <- 1 to 1000) {
      
      Ticket.reset
      
      val c = new Conductor
      import c.thread
      import c.whenFinished
      import c.waitForBeat
      
      tA = null
      tB = null
      tC = null
    
      successA = true
      successB = true
      successC = true

      thread("A") {
        tA = Ticket.takeTicket
        // println("A's ticket: " + tA.getTicketNum)
//        println("first union call: A")
        waitForBeat(1)
        waitForBeat(2)
        tB should not be (null)
        successA = tA.union(tB)
      }
      
      thread("B") {
        tB = Ticket.takeTicket
        // println("B's ticket: " + tB.getTicketNum)
        waitForBeat(1)
        waitForBeat(2)
//        println("first union call: B")
        tC should not be (null)
        successB = tB.union(tC)
      }
      
      thread("C") {
        tC = Ticket.takeTicket
        // println("C's ticket: " + tC.getTicketNum)
        waitForBeat(1)
        waitForBeat(2)
//        println("first union call: C")
        tA should not be (null)
        successC = tC.union(tA)
      }
      
      whenFinished{
//      println("successA: %b, successB: %b, successC: %b"
//      .format(successA, successB, successC))
        (!successA || !successB || !successC) should be (true) 
      }
      
    }
  }
  
  }  
  
}


