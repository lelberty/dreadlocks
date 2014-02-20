package dreadlocks.core

import scala.collection.concurrent.TrieMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class DreadLockException(msg : String) extends Exception(msg)

object DreadLockStatus extends Enumeration {
  type DreadLockStatus = Value
  val Success, Deadlock, ERROR = Value
}

/* Scala implementation of a DreadLock
 * @author lelberty
 */
class DreadLock() {
  
  import dreadlocks.core.DreadLockStatus.Value
  
  val BITMAP_SIZE = 64
  
  case class LockData(locked : AtomicBoolean, owner : AtomicReference[Ticket])
  
  val resourceMap : TrieMap[Any, LockData] = new TrieMap[Any, LockData]
  
  def reset() : Unit = {
    Ticket.reset();
    resourceMap.clear
  }
  
  def getLockData(key : Any) : LockData = {
    resourceMap.getOrElseUpdate(key, 
      new LockData(new AtomicBoolean(false), new AtomicReference(null)))
  } 

  def lock(key : Any) : DreadLockStatus.Value = {
        
    val myTicket : Ticket = Ticket.takeTicket()
    val tn = myTicket.getTicketNum()
    
    val lockData : LockData = getLockData(key)
    
    try {
      
      var lastOwner : Ticket = null
      
      while(true) {
        while (lockData.locked.get()) {
          val curOwner = lockData.owner.get()

          if (myTicket.cycleCheck()) {
            return DreadLockStatus.Deadlock 
          }
          else if (curOwner != null) { 
            // no cycle, update my digest if the owner has changed
            
            if (curOwner != lastOwner) {
              myTicket.remove(lastOwner)
              myTicket.union(curOwner)
            }
          }
          lastOwner = curOwner          
        }
        
        // unlocked, attempt to acquire
        if (lockData.locked.compareAndSet(false, true)) {
          // safely update the owner
          if (!lockData.owner.compareAndSet(lockData.owner.get(), myTicket))
            throw new DreadLockException("owner was changed even though I have the lock")

          return DreadLockStatus.Success
        }
      }
      
      return DreadLockStatus.ERROR
    } catch {
      case e: Exception => throw e
    }
    
  }
  
  def unlock(key : Any): Unit = {
    
    val lockData : LockData = getLockData(key)
    lockData.owner.set(null)
        
    // return my ticket
    val ticketFree = Ticket.returnTicket()
    lockData.locked.set(false)
  }
}
