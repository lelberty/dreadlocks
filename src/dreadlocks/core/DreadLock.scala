package dreadlocks.core
import LockStatus.LockStatus
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class DreadLockException(msg : String) extends Exception(msg)

/** DreadLock companion object
 * Currently, the tickets and digests are being tracked import dreadlocks.core.LockStatus
import dreadlocks.core.Ticket
as static members of
 * the companion object. This may need to be changed for performance reasons
 * 
 * //TODO: Figure out proper parameters for ConcurrentHashMap, or come up with 
 * better way to distribute tickets
 */
object DreadLock {
    
  // size of digest bitmaps
  val BITMAP_SIZE = 64

  var enableDetection = true
  var backoff = 0
  var dlBackoff = 0
    
  def setEnableDetection(enable:Boolean) = enableDetection = enable
  def setBackoff(backoff:Int) = DreadLock.backoff = backoff
  def setDLBackoff(backoff:Int) = DreadLock.backoff = backoff
}

/* Scala implementation of a DreadLock
 * @author lelberty
 */
class DreadLock() {
  
  case class LockData(locked : AtomicBoolean, ownerTicket : AtomicReference[Ticket], waiters : ConcurrentLinkedQueue[Thread])
  
  val keyMap = new ConcurrentHashMap[Any, LockData]()
    
  private def lock(key : Any) : LockStatus = {
    
    val myTicket : Ticket = Ticket.takeTicket() 

    // get the LockData for this key
    val lockData = keyMap.get( key ) match {	// 
      case null =>
        val ld = new LockData(
              new AtomicBoolean(false), 
              new AtomicReference[Ticket](myTicket),
              new ConcurrentLinkedQueue[Thread]())
        keyMap.putIfAbsent( key, ld)
        ld
      case ok: LockData => ok
    }

    var wasInterrupted : Boolean = false     // was I interrupted as I was waiting?
    val current = Thread.currentThread()

    lockData.waiters.add(current) // I'm next in line
    
    // now try acquiring the lock
    try {
      
      // block if I'm not the next in line, or if the lock isn't available
      while (lockData.waiters.peek() != current || 
             !lockData.locked.compareAndSet(false, true)) {
        
        // Update owner, but if I already own it then cut out early because we don't have to do any more work  
        if (lockData.ownerTicket.getAndSet(myTicket) == myTicket) return LockStatus.alreadyOwned
        
        // this will attempt to detect a cycle. If it misses it, it will be caught later
        if (!myTicket.union(lockData.ownerTicket.get)) return LockStatus.deadlock
        
        // if no deadlock, then park on this key 
        LockSupport.park(key)
        
        if (Thread.interrupted()) wasInterrupted = true // ignore interrupts while waiting
      }
      
      // If I get the lock, then interrupt if I was interrupted while waiting
      if (wasInterrupted) Thread.currentThread().interrupt()
      
      return LockStatus.deadlock
    } catch {
      case e : Exception => throw(e) 
    }
            
  }

  def lock() : LockStatus = lock(false)
  def tryLock() : LockStatus = lock(true)

  def unlock(key : Any): Unit = {
    
    val lockData = keyMap.get( key ) match {	// 
      case null => throw new DreadLockException("key -> lockData not found on unlock for key: " + key.toString)
      case ok: LockData => ok
    }
    
    // return my ticket
    val ticketFree = Ticket.returnTicket()
    
//    if (ticketFree) 
    
    // unset owner
//    if !lockData.ownerTicket.compareAndSet(, arg1)
//    
//    // unlock the lock
//    
//    val next = lockData.waiters.poll()
//    LockSupport.unpark(next)
  }
}



