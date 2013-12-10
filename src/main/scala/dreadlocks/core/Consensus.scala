package dreadlocks.core

import java.util.concurrent.atomic.AtomicReference

class Consensus[T]() {

  private val winner : AtomicReference[T] = new AtomicReference[T]()

//  def decide(v : T) : T = {
//    val existing : T = 
//    winner.compareAndSet(existing, v)
//    winner.get()
//  }

}
