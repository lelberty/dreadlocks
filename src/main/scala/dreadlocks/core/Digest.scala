package dreadlocks.core

import java.util.BitSet
import scala.collection.mutable.HashSet 

/* @author lelberty
 * 
 * Digest used in DreadLock.
 */
class Digest(size: Int) {

  var bits = new BitSet(size);

  // note that this will always be  flat
  var digests = new HashSet[Digest]
  
  var internalSet = new HashSet[Digest]
  internalSet.add(this) // always contains self
  
  def getInternalSet() = internalSet
  
  def contains(index: Int) : Boolean = contains(index, new HashSet[Digest]) 
  
  def cycleCheck() : Boolean = false // TEMPORARY. BAD.
  
  /* contains must check each internal set */
  private def contains(index: Int, visited: HashSet[Digest]) : Boolean = synchronized {
    if (visited.contains(this)) {
      false // bits.get(index)
    } else {
//      try{
        visited.add(this)
        val bitsHasIt = bits.get(index)
        val setHasIt = 
        internalSet.exists( (d) =>
        try {
          d.contains(index,visited)
        } catch {
          case e:Exception => 
          println(e)
          println(internalSet)
          println("After: %s".format(d))
          // println("Throwing" + e)
          throw e
        })
        bitsHasIt || setHasIt
//      } catch{
//        case e:Exception =>
//          println("GETTING HERE")
//          println(e)
//          println("Index: %d, Visited: %s, bits: %s, internalSet: %s".format(index, visited, bits, internalSet))
//          throw e
//      }
    }
  } 
  
  def containsAll(indices : List[Int]) : Boolean = indices.forall( (i) => contains(i) )
  def containsNone(indices : List[Int]) : Boolean = indices.forall( (i) => !contains(i) )
      
  def isEmpty() : Boolean = containsNone(List.range(0,size))
      
  def containsOnly(indices : List[Int]) : Boolean = {
    val allBits = List.range(0, size) // assuming here that the size of each bitset is the same
    allBits.forall ( (i) =>  (indices.contains(i.toInt) match {
      case true => contains(i)
      case false => !contains(i)
    }) 
    && indices.forall( (i) => i < size) )
  } 
  
  def containsOnly(index : Int) : Boolean = containsOnly(List(index))
  
  def union(other: Digest) : Unit = synchronized { internalSet.add(other) }  
  
  // note that you cannot set to 0, as that would affect all the linked digests 
  def setIndex(index : Int) = synchronized { bits.set(index) }
  
  def clear() = synchronized{ bits.clear(); internalSet.clear(); internalSet.add(this) }

  def setIndices(indices : List[Int]) = {
    indices.foreach(setIndex)
  }
  
  private def toSingleBitSet(visited : HashSet[Digest]) : BitSet = {
    if (!visited.contains(this)) {
      visited.add(this)
      internalSet.foldLeft(this.bits)((acc, d) => {
        acc.or(d.toSingleBitSet(visited))
        acc
      })
    }
    else new BitSet() 
  }
  
  private def toSingleBitSet() : BitSet = toSingleBitSet(new HashSet[Digest])

    internalSet.foldLeft(new BitSet(size))( 
        (acc, d) => {
          if (d.eq(this)) acc.or(d.bits)
          else acc.or(d.toSingleBitSet())
          acc } )

  override def toString() : String = toSingleBitSet().toString() 
  
  //TODO: override hashcode
  
  
//    
//    internalSet.foldLeft("")(
//      (acc, d) => acc + d.toString())
    
//    toSingleBitSet().toString() 
  
}
