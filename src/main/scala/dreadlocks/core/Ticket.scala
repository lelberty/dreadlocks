package dreadlocks.core

import scala.collection.mutable.SynchronizedStack
import scala.collection.immutable.List
import scala.collection.concurrent.TrieMap
//import java.util.concurrent.ConcurrentHashMap
import scala.collection.immutable.BitSet
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock;

/* Manages access to the DreadLock.
 * Every thread attempting to acquire a lock must acquire a ticket.
 */
object Ticket {
  
  object TicketStatus extends Enumeration {
    type status = Value
    val READY, RECYCLING = Value
  }
  
  val TOTAL_TICKETS = 64
  val ALL_TICKETS = List.range(0, TOTAL_TICKETS)

  val gLock : ReentrantLock = new ReentrantLock()
  
  // for recycling tickets
  // val ticketStack : SynchronizedStack[Ticket] = new SynchronizedStack[Ticket]()
  
  // no recycling; creating new Tickets EVERY time one is pulled off of the stack 
  // TODO: add recycling
  val ticketNumStack : SynchronizedStack[Int] = new SynchronizedStack[Int]()
  
  val ticketMap : TrieMap[Int, Ticket] = new TrieMap[Int, Ticket]()
  
  def reset() : Unit = {
    ticketNumStack.clear()
    ticketMap.clear()
    // alloc and push all tickets into the stack
    ALL_TICKETS.foreach( (n : Int) => ticketNumStack.push(n))
  }
  
  reset()
  
  // assigns a new ticket if the Thread does not currently own a ticket, otherwise 
  // gets currently owned Ticket 
  def takeTicket() : Ticket = {
    
    val tid = Thread.currentThread().getId().toInt
    
    val myTicket:Ticket = ticketMap.get(tid) match {
      case None =>
        try {
          val t = new Ticket(ticketNumStack.pop())
          ticketMap.put(tid, t)
          t.incrementHoldCount
          t
        } catch {
          case e: Exception => throw new DreadLockException(
            "Failed to take Ticket: " + e.getMessage()) 
        }
      case Some(t : Ticket) =>
        t.incrementHoldCount
        t
    }
    if (myTicket == null) throw new NullPointerException("takeTicket returned null ticket")
    myTicket
  }
  
  // returns false if the ticket's holdCount is zero after returning the ticket
  def returnTicket() : Boolean = {
    
    val tid = Thread.currentThread().getId().toInt
    
    val myTicket:Ticket = ticketMap.get(tid) match {
      case None => throw new DreadLockException("thread + %d has no Ticket".format(tid))
      case Some(t:Ticket) => t
    }

    myTicket.decrementHoldCount()
    
    if (myTicket.isFree()) {
      ticketMap.remove(tid)
      myTicket.clear()
      ticketNumStack.push(myTicket.getTicketNum())
      return false
    } else return true
  }
  
}

class Ticket(ticketNum : Int) {

  private var holdCount : Int = 0 

  // bits does NOT contain self, so that deadlock checks can be cache-local and insanely fast
  @volatile private var bits : BitSet = BitSet.empty
  
  /* dependents should NEVER contain this Ticket */
  @volatile private var dependents : List[Ticket] = List[Ticket]()
  
  @volatile private var deadlocked : Boolean = false
  
  private def incrementHoldCount() : Unit = holdCount = holdCount + 1
  
  private def decrementHoldCount() : Unit = { 
    holdCount = holdCount - 1
    if (holdCount < 0) throw new DreadLockException("holdCount for ticket " + ticketNum + " went below 0")
  }
  
  def getTicketNum() : Int = ticketNum
  
  def getHoldCount() : Int = holdCount
  
  def getDependents() = dependents
  def getBits() = bits
  
  def isFree() : Boolean = holdCount == 0
  
  def clear() : Unit = {
    clearBits()
    dependents = List.empty[Ticket]
  }
  
  def clearBits() : Unit = bits = BitSet.empty

  def union(other: Ticket) : Unit = _union(other, List[Ticket]())

  // lock-free union
  private def _union(other: Ticket, visited : List[Ticket]) : Unit = {
     // println("%d U %d".format(ticketNum, other.getTicketNum()))
    // deadlock, so flag everyone in the cycle
    if (visited.contains(this)) {
      visited.foreach( (t) => t.deadlocked = true )
    } else {
      val oDependents = other.dependents
      other.dependents = this::oDependents
      val newVisited = this :: visited
      dependents.foreach( (t) => t._union(this, newVisited) )
    }
  }

  def cycleCheck() : Boolean = deadlocked

  override def toString() : String = {
    "Ticket[ticketNum = %d, bits = %s, dependents = %s]"
		.format(ticketNum, bits, dependents)
  }
}
