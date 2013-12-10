package dreadlocks.core

import scala.collection.mutable.BitSet
import java.util.concurrent.locks.ReentrantLock

/*
 * Provides safe accesses to a bitset under the hood
 */
class ConcurrentBitSet(length:Int) {
  
  private val bs:BitSet = new BitSet(length)
  private val lock:ReentrantLock = new ReentrantLock()
    
  def +=(elem: Int): Unit = lock.synchronized { bs += elem }
  
  def -=(elem: Int): Unit = lock.synchronized { bs -= elem }
  
  private def getInternalBitSet() : BitSet = lock.synchronized {bs} 

  def |(other: ConcurrentBitSet) : Unit = lock.synchronized {
    bs | other.getInternalBitSet
  }
  
  def contains(elem: Int): Boolean = lock.synchronized {bs.contains(elem)}
  
  def clear(): Unit = lock.synchronized {bs.clear}
  
  def isEmpty(): Boolean = lock.synchronized {bs.isEmpty}

  override def toString(): String = bs.toString()
}
