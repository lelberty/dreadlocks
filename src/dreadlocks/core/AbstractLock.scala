package dreadlocks.core
import java.util.concurrent.ConcurrentHashMap
import scala.concurrent._
import scala.math.Ordering.Implicits._
import dreadlocks.core.DreadLock

object LockStatus extends Enumeration {
  type LockStatus = Value
  val success, alreadyOwned, deadlock, other = Value
}

class AbstractLockException(msg : String) extends Exception(msg)

/**
 * @author mph
 * Transactional Boosting abstract lock
 */
object AbstractLock {
  
  var dlCount = 0
  var successCount = 0
  
  def reset(): Unit = {
    dlCount = 0
    successCount = 0
  }
  
  def getDlCount() = dlCount
  def getSucessCount() = successCount
  
  val lockMap = new ConcurrentHashMap[ Any, DreadLock ]()
      
  def lock( key: Any ): Boolean = {

    // make sure this key is bound to a lock
    val myLock = lockMap.get( key ) match {	// 
      case null => lockMap.putIfAbsent( key, new DreadLock ); lockMap.get( key )
      case ok: DreadLock => ok
    }
    
    var res = myLock.lock()

    while(res.equals(LockStatus.deadlock)) {
      dlCount = dlCount + 1
      // res = myLock.lock()
      return false // give up - we're not worried about HOW to handle deadlock
    } 
    
    if (res.equals(LockStatus.alreadyOwned)) return false
    return true
  }
  
  
  def unlock( key: Any): Unit = {
    
    val myLock = lockMap.get(key) match {
      case null => throw new AbstractLockException("During unlock, lock was not found for key: %s".format(key))
      case ok: DreadLock => ok
    }
    
    myLock.unlock()

  }

}
