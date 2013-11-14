package dreadlocks.core

import scala.collection.mutable.SynchronizedStack
import scala.collection.immutable.List
import java.util.concurrent.ConcurrentHashMap
import scala.collection.immutable.BitSet
import dreadlocks.core.DreadLockException

/* Manages access to the DreadLock.
 * Every thread attempting to acquire a lock must acquire a ticket.
 * This ticket consists of a unique id   
 */
object Ticket {
  
  val TOTAL_TICKETS = 64
  val ALL_TICKETS = List.range(0, TOTAL_TICKETS)
  val ticketStack : SynchronizedStack[Ticket] = new SynchronizedStack[Ticket]()
  
  val ticketMap : ConcurrentHashMap[Int, Ticket] = new ConcurrentHashMap[Int, Ticket]()
    
  def reset() : Unit = {
    ticketStack.clear
    ticketMap.clear
    // alloc and push all tickets into the stack
    ALL_TICKETS.foreach( (n : Int) => ticketStack.push(new Ticket(n)))
  }
  
  reset()
  
  // assigns a new ticket if the Thread does not currently own a ticket, otherwise 
  // gets currently owned Ticket 
  def takeTicket() : Ticket = {
    
    val tid = Thread.currentThread().getId().toInt
        
    val myTicket:Ticket = ticketMap.get(tid) match {
      case null =>
        try {
          val t = ticketStack.pop()
          assert(t.bits.isEmpty)
          assert(t.dependents.isEmpty)
          ticketMap.put(tid, t)
          t.incrementHoldCount
          t
        } catch {
        case e: Exception => throw new DreadLockException(
            "Failed to take Ticket: " + e.getMessage()) 
        }
      case ok:Ticket =>
        assert(ok.bits.isEmpty)
        assert(ok.dependents.isEmpty)
        ok.incrementHoldCount
        ok
    }
    if (myTicket == null) throw new NullPointerException("takeTicket returned null ticket")
    myTicket
  }
  
  // returns false if the ticket's holdCount is zero after returning the ticket
  def returnTicket() : Boolean = {
    
    val tid = Thread.currentThread().getId().toInt
    
    val myTicket:Ticket = ticketMap.get(tid) match {
      case null => throw new DreadLockException("thread + %d has no Ticket".format(tid))
      case t:Ticket => t
    }
    
    myTicket.decrementHoldCount
    
    if (myTicket.isFree) {
      ticketMap.remove(tid)
      myTicket.reset
      ticketStack.push(myTicket)
      return false
    } else return true
  }
    
}

class Ticket(ticketNum : Int) {
  
  // needs to be volatile? probably not because only 
  // the one thread using Ticket is ever concerned with its value
  private var holdCount : Int = 0 
  	// 
  // bits does NOT contain self, so that deadlock checks can be cache-local and insanely fast
  @volatile private var bits : BitSet = BitSet.empty
  
  /* dependents should NEVER contain this Ticket */
  @volatile private var dependents : List[Ticket] = List[Ticket]()
  
  // NOTE: These may need to be synchronized, or at the very least the holdCount var will need to be 'volatile'
  private def incrementHoldCount() : Unit = holdCount = holdCount + 1
  
  private def decrementHoldCount() : Unit = { 
    holdCount = holdCount - 1
    if (holdCount < 0) throw new DreadLockException("holdCount for ticket " + ticketNum + " went below 0")
  }
  
  def getTicketNum() : Int = ticketNum
  
  def getHoldCount() : Int = holdCount
  
  def isFree() : Boolean = holdCount == 0
  
  // NOTE: I believe concurrent calls to clear and union can cause false positives
  def clear() : Unit = { 
    bits = BitSet.empty
    dependents = List.empty[Ticket]
  }
  
  def reset() : Unit = {
    this.clear()
  }
  
  def union(other: Ticket) : Boolean = {
    
    // println("union, this.ticketNum = %d".format(this.ticketNum))
    
    if (other == null) throw new Exception("blarg")

    bits = bits + other.getTicketNum // I'm dependent on other 
    bits = bits | other.bits // and everything it's dependent on
    
//    println("Before cycle check: %d's bits: %s".format(this.ticketNum, this.bits))

    // cycle detected, abort. NOTE: even if this is missed because of
    // concurrent calls to union, it will eventually be caught by someone in the cycle
    if (this.cycleCheck()) return false
    
    other.dependents = this::other.dependents
    
//    println("%d's dependents: %s, isEmpty: %b"
//          .format(ticketNum, dependents, dependents.isEmpty))

    // back-propagate - everyone dependent on me should be
    // again unioned with me, and return false if a cycle was detected
    return dependents.forall((t : Ticket) => {
//      println("back-propagating %d's change to %d".format(ticketNum, t.getTicketNum))
      t.union(this) })
  } 
  
  // manually check for a cycle
  // because bits ONLY contains my ticketNum if there's a cycle, this
  // can be checked just by looking at my dependencies. This provides
  // speed and cache-locality
  def cycleCheck() : Boolean = {
    val res = bits(ticketNum)
//    println("cc | bits = %s, ticketNum: %d, res: %b"
//        .format(bits, ticketNum, res))
    res
  }

      
}
