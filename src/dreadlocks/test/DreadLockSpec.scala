package dreadlocks.test

import dreadlocks.core._

import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.concurrent._
import org.scalatest.FunSpec

class DreadLockSpec extends FunSpec {
  describe("DreadLock") {
    it ("should work") {}
  }
}

//package scala.concurrent.tko.nostm.tests
//
//import scala.concurrent.tko.nostm.locks._
//import org.scalatest.FunSpec
//import org.scalatest.matchers.ShouldMatchers._
//import org.scalatest.concurrent._
//import java.util.concurrent.ConcurrentHashMap
//import java.util.concurrent.atomic.AtomicInteger
//import java.util.concurrent.atomic.AtomicReference
//
//class DreadLockSpec extends FunSpec {
//
//  /*** DreadLock Tests ***/
//
//  describe("DreadLock") {
//
//    it ("should correctly acquire and " +
//        "release a lock for a single thread") {
//
//      DreadLock.reset()
//
//      val conductor = new Conductor
//      import conductor._
//
//      val d = new DreadLock()
//
//      var a_id = -1
//
//      thread("A") {
//
//        d.lock() should be (LockStatus.success)
//        
//        // A should have a ticket
//        var a_ticket = getTicket
//        var a_digest = getDigest
//
//        // ticket pool should contain all tickets but a's ticket
//        DreadLock.getAllTickets().filterNot((t) => t == a_ticket).foreach(
//            (t) => DreadLock.getTicketPool() contains(t) should be (true) )
//
//        // A's digest should only contain its own ticket
//        a_digest.containsOnly(a_ticket) should be (true)
//
//        // The owner digest should be A's digest
//        a_digest should be theSameInstanceAs d.getOwnerDigest()
//
//        d.unlock()
//
//        // the owner digest should not have been updated
//        d.getOwnerDigest() should be (null)
//
//      }
//
//      whenFinished {
//        beat should be (0)
//        DreadLock.reset()
//      }
//
//    }
//    
//    it ("should correctly manage multiple lock attempts by the same thread") {
//
//      DreadLock.reset()
//
//      var d = new DreadLock()
//
//      val c = new Conductor
//      import c._
//
//      var a_id = -1 
//
//      thread("A") {
//        a_id = getTid()
//            
//        // lock once
//        d.lock() should be (LockStatus.success)
//        
//        val a_ticket = getTicket
//        val a_digest = getDigest
//
//        val od = d.getOwnerDigest()
//
//        od.containsOnly(a_ticket) should be (true)
//        a_digest should be theSameInstanceAs od
//
//        // lock twice
//        d.lock() should be (LockStatus.alreadyOwned)
//
//        od.containsOnly(a_ticket) should be (true)
//        a_digest should be theSameInstanceAs od
//
//      }
//
//      whenFinished {
//        beat should be (0)
//        DreadLock.reset()
//      }
//
//    }
//
//    it ("should correctly handle two contending threads") {
//      
//      DreadLock.reset()
//      
//      val c = new Conductor
//      import c._
//      
//      var b_ticket, a_ticket, b_holdCount, a_holdCount : AtomicInteger = new AtomicInteger
//      var b_digest, a_digest: AtomicReference[Digest] = new AtomicReference[Digest]
//      
//      val d = new DreadLock()
//
//      thread("A") {
//        
//        a_digest.set(getDigest)
//        
//        d.lock() should be (LockStatus.success)
//        
//        getHoldCount should be (1)
//        
//        waitForBeat(1) // let B try to acquire the lock
//        
//        // the owner digest should still be A's digest
//        getDigest should be theSameInstanceAs d.getOwnerDigest()
//
//        d.unlock()
//        
//        waitForBeat(3)
//        
//        getHoldCount should be (-1)
//        getTicket should be (-1)
//        getDigest() should be (null)
//      }
//
//      thread("B") {
//        
//        b_digest.set(getDigest)
//        
//        waitForBeat(1) // wait until A has acquired lock
//
//        // fails, waits, then acquires
//        d.lock() should be (LockStatus.success)
//        
//        getHoldCount should be (1)
//        
//        waitForBeat(3)
//        
//        // digest should have reset to just contain B after A unlocked
//        getDigest.containsOnly(getTicket) should be (true)
//        a_digest.get() should be (null) // A has cleared all of its stuff
//
//        d.unlock()
//        
//        getHoldCount should be (-1)
//        getTicket should be (-1)
//        getDigest should be (null)
//      }
//      
//      whenFinished {
//        
//        // everything unlocked, all tickets should be returned
//        DreadLock.ALL_TICKETS.forall( (i) => DreadLock.getTicketPool.contains(i))
//        
//        // ownerDigest should be nullified
//        d.getOwnerDigest should be (null)
//        DreadLock.reset
//      }
//    }
//    
//    it ("should correctly detect a simple 2-thread deadlock") {
//      
//      DreadLock.reset()
//      DreadLock.setEnableDetection(true)
//
//      val c = new Conductor
//      import c._
//      
//      var b_ticket, a_ticket, b_holdCount, a_holdCount : AtomicInteger = new AtomicInteger()
//      var b_digest, a_digest: AtomicReference[Digest] = new AtomicReference[Digest]
//
//      val d1 = new DreadLock()
//      val d2 = new DreadLock()
//
//      thread("A") {
//        
//        
//        d1.lock() should be (LockStatus.success)
//        
//        a_digest.set(getDigest)
//        a_ticket.set(getTicket)
//        
//        getHoldCount should be (1)
//        
//        a_ticket.set(getTicket)
//        
//        d1.getOwnerDigest should be theSameInstanceAs getDigest
//
//        waitForBeat(1)
//        
//        d2.lock() should be (LockStatus.success)
//        
//        getHoldCount should be (2)
//
//        b_ticket.get should not be (-1)
//        b_digest.get should not be (null)
//        a_digest.get().containsOnly(getTicket) should be (true)
//        b_digest.get().containsOnly(b_ticket.get) should be (true)
//
//        d1.getOwnerDigest() should be theSameInstanceAs a_digest.get
//        d2.getOwnerDigest() should be theSameInstanceAs a_digest.get
//
//        d2.unlock
//        d1.unlock
//        
//        getTicket should be (-1)
//        getDigest should be (null)
//        getHoldCount should be (-1)
//      }
//
//      thread("B") {
//                
//        d2.lock() should be (LockStatus.success)
//        
//        b_digest.set(getDigest)
//        b_ticket.set(getTicket)
//        getHoldCount should be (1)
//        
//        d2.getOwnerDigest should be theSameInstanceAs getDigest
//
//        waitForBeat(2)
//        
//        // A's ticket, digest should be set
//        a_ticket.get should not be (-1)
//        a_digest.get should not be (null)
//        
//        // A's digest should contain both tickets
//        a_digest.get().containsOnly(List(a_ticket.get, getTicket)) should be (true)
//        
//        // B's digest should only contain B's ticket
//        getDigest().containsOnly(getTicket) should be (true)
//        
//        d1.getOwnerDigest() should be theSameInstanceAs a_digest.get
//
//        d1.lock() should be (LockStatus.deadlock) // deadlock!
//
//        getDigest().containsOnly(getTicket) should be (true)
//        a_digest.get().containsOnly(List(a_ticket.get, getTicket)) should be (true)
//        d1.getOwnerDigest() should be theSameInstanceAs a_digest.get
//
//        d2.unlock() // let A go through
//        
//        getTicket should be (-1)
//        getDigest should be (null)
//        getHoldCount should be (-1)
//
//      }
//
//      whenFinished {
//        beat should be (2)
//        d1.getOwnerDigest should be (null)
//        d2.getOwnerDigest should be (null)
//        DreadLock.reset()
//      }
//
//    }
//
//    it ("should be able to detect a deadlock between three threads") {
//      
//
//      DreadLock.reset()
//
//      val c = new Conductor
//      import c._
//
//      var a_id, b_id, c_id, a_ticket, b_ticket, c_ticket 
//        : AtomicInteger = new AtomicInteger
//      var a_digest, b_digest, c_digest : AtomicReference[Digest] = new AtomicReference[Digest]
//
//      val d1 = new DreadLock()
//      val d2 = new DreadLock()
//      val d3 = new DreadLock()
//
//      thread("A") {
//
//        d1.lock() should be (LockStatus.success)
//
//        a_ticket.set(getTicket)
//        a_digest.set(getDigest)
//        
//        getHoldCount should be (1)
//
//        waitForBeat(1)
//
//        d2.lock() should be (LockStatus.success)
//        
//        getHoldCount should be (2)
//
//        // once a has gotten d2:
//
//        d2.getOwnerDigest() should be theSameInstanceAs getDigest 
//        d1.getOwnerDigest() should be theSameInstanceAs getDigest
//
//        // finally, unlock d1
//        d1.unlock()
//        
//        d2.unlock() // and release d2
//      }
//
//      thread("B") {
//
//        d2.lock() should be (LockStatus.success)
//        
//        getHoldCount should be (1)
//
//        b_ticket.set(getTicket)
//        b_digest.set(getDigest)
//
//        waitForBeat(2)
//
//        // at this point A is waiting on d2
//
//        a_digest.get().containsOnly(List(a_ticket.get, getTicket)) should be (true)
//        d1.getOwnerDigest should be theSameInstanceAs a_digest.get
//
//        getDigest.containsOnly(getTicket) should be (true)
//        d2.getOwnerDigest should be theSameInstanceAs getDigest
//
//        c_digest.get().containsOnly(c_ticket.get) should be (true)
//        d3.getOwnerDigest should be theSameInstanceAs c_digest.get
//
//        d3.lock should be (LockStatus.success)
//        
//        getHoldCount should be (2)
//
//        // once d3 has been acquired 
//
//        d2.getOwnerDigest() should be theSameInstanceAs getDigest
//        d3.getOwnerDigest() should be theSameInstanceAs getDigest
//
//
//        d2.unlock() // let a through
//        
//        d3.unlock() // and release d3
//        
//        getTicket should be (-1)
//        getHoldCount should be (-1)
//        getDigest should be (null)
//
//      }
//
//      thread("C") {
//
//        d3.lock() should be (LockStatus.success)
//        
//        getHoldCount should be (1)
//
//        c_ticket.set(getTicket)
//        c_digest.set(getDigest)
//
//        // make sure this happens before beat 1 
//        waitForBeat(1)
//
//        // after a has tried locking d2, and b has tried locking d3, cause deadlock
//        waitForBeat(3) 
//
//        a_digest.get().containsOnly(List(a_ticket.get, b_ticket.get, getTicket)) should be (true)
//        b_digest.get().containsOnly(List(b_ticket.get, getTicket)) should be (true)
//        getDigest.containsOnly(c_ticket.get) should be (true)
//
//        d1.getOwnerDigest() should be theSameInstanceAs a_digest.get
//        d2.getOwnerDigest() should be theSameInstanceAs b_digest.get
//        d3.getOwnerDigest() should be theSameInstanceAs getDigest
//
//        d1.lock() should be (LockStatus.deadlock) // deadlock!
//        
//        getHoldCount should be (1)
//
//        // after failing because of deadlock, nothing changed
//
//        a_digest.get().containsOnly(List(a_ticket.get, b_ticket.get, getTicket)) should be (true)
//        b_digest.get().containsOnly(List(b_ticket.get, getTicket)) should be (true)
//        getDigest.containsOnly(c_ticket.get) should be (true)
//
//        d1.getOwnerDigest() should be theSameInstanceAs a_digest.get
//        d2.getOwnerDigest() should be theSameInstanceAs b_digest.get
//        d3.getOwnerDigest() should be theSameInstanceAs getDigest
//
//        d3.unlock() // let b go through, which triggers chain reaction
//
//      }
//
//      whenFinished {
//        beat should be (3)
//        // there should be no remaining digests
//        d1.getOwnerDigest should be (null)
//        d2.getOwnerDigest should be (null)        
//        d3.getOwnerDigest should be (null)
//        
//        DreadLock.reset()
//      }
//
//    }
//
//    it ("should properly maintain holdCount for a single thread and single lock") {
//
//      DreadLock.reset()
//
//      val c = new Conductor
//      import c._
//
//      val d1 = new DreadLock()
//
//      thread("A") {
//
//
//        d1.lock() should be (LockStatus.success)
//
//        getHoldCount should be (1)
//
//        d1.lock() should be (LockStatus.alreadyOwned)
//
//        getDigest().containsOnly(getTicket) should be (true)
//
//        // shouldn't increment after a second attempt
//        getHoldCount should be (1)
//
//        d1.unlock()
//
//        // should decrement on unlock
//        getHoldCount should be (-1)
//
//        // additionally, tickets and digests should be cleared, and ticket pool full
//        getDigest should be (null)
//        DreadLock.ALL_TICKETS.forall((t) => DreadLock.getTicketPool().contains(t))
//        getTicket should be (-1)
//
//      }
//
//      whenFinished {
//        beat should be (0)
//        DreadLock.reset()
//      }
//
//    }
//
//    it ("should correctly manage holdCount for one thread and many resources") {
//
//      DreadLock.reset()
//
//      val c = new Conductor
//      import c._
//      
//      val d1 = new DreadLock()
//      val d2 = new DreadLock()
//      val d3 = new DreadLock()
//
//      thread("A") {
//
//        getHoldCount should be (-1)
//
//        d2.lock() should be (LockStatus.success)
//
//        getHoldCount should be (1)
//
//        d2.lock() should be (LockStatus.alreadyOwned)
//        
//        getHoldCount should be (1)
//
//        d1.lock() should be (LockStatus.success)
//
//        getHoldCount should be (2)
//
//        d2.unlock()
//
//        getHoldCount should be (1)
//
//        d2.lock()
//        d2.unlock()
//        getHoldCount should be (1)
//        d2.lock()
//        d1.unlock()
//        getHoldCount should be (1)
//        d2.lock()
//        d2.lock()
//        d1.lock()
//        d2.lock() should be (LockStatus.alreadyOwned)
//
//        getHoldCount should be (2)
//      }
//
//      whenFinished {
//        beat should be (0)
//        DreadLock.reset()
//      }
//
//    }
//    
//    
//
//  }
//
//
//  /*** Testing helper methods ****/
//
////  def getTicketOrFail(tid : Int) : Int = {
////      DreadLock.getMyTicket.get()
////      }    
////  }
////
////  def getDigestOrFail(ticket : Int) : Digest = {
////      DreadLock.getDigests().get(ticket) match {
////      case d:Digest => d
////      case null => fail()
////      }
////  }
//  
//  def getTicket() : Int = {
//    DreadLock.getMyTicket.get
//  }
//  
//  def getDigest() : Digest = {
//    DreadLock.getMyDigest.get
//  }
//  
//  def getHoldCount() : Int = {
//    DreadLock.getMyHoldCount.get
//  }  
//  
//
//  def getTid() : Int = Thread.currentThread().getId().toInt
//
//      /*** END Testing helper methods ***/
//
//
//}
//
//
//
