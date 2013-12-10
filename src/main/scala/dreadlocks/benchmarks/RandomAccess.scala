package dreadlocks.benchmarks

import scala.util.Random
import scala.collection.mutable.ListBuffer
import java.util.concurrent.CyclicBarrier
import dreadlocks.core.DreadLock
import dreadlocks.core.AbstractLock
//import dreadlocks.benchmarks.DummyData

object RandomAccess {
  
  // Parameters
  var threads:Int = 2
  var warmups:Int = 2000
  var runs:Int = 100
  var numElem:Int = 100
  var numAccesses:Int = numElem
  var batchSize:Int = 2
  var nextBatchSize:Int = batchSize
  var verbose:Boolean = false
        
  private var barrier:CyclicBarrier = null
  def getBarrier() = barrier
  
  private var runBarrier:CyclicBarrier = null
  def getRunBarrier() = runBarrier
  
  private var contentionList:List[DummyData] = null
  def getContentionList() = contentionList
  
  def main(args:Array[String]): Unit = {
    
     parseArgs(args)
     
     var time:Long = 0;
     var total:Long = 0;
     var avg:Long = 0;

     contentionList = List.tabulate(numElem)(
         (n) => new DummyData(n))
         
         
     for (i <- 1 to warmups) {
       AbstractLock.reset()
       time = runRandomAccess()
           total += time
       if (verbose) println("Warmup %d completed in: %d, deadlocks: %f"
           .format(i, time, AbstractLock.getDlCount()/(1.0*numAccesses*threads)))
     }
     
     avg = total/warmups
     
     println("Average warmup time: %d".format(avg))
     avg = 0
     total = 0
     
     for (i <- 1 to runs) {
       AbstractLock.reset()
       time = runRandomAccess()
       total += time
       if (verbose) println("Run %d completed in: %d, deadlocks/runs: %f"
         .format(i, time, AbstractLock.getDlCount/(1.0*numAccesses*threads)))
     }
     
     avg = total/runs
     
     println("Average run time: %d".format(avg))
     avg = 0
     total = 0
  }

  private def runRandomAccess(): Long = {
    
    barrier = new CyclicBarrier(threads)
    runBarrier = new CyclicBarrier(threads+1)
    
    // create list

    // spawn processes
    for (i <- 1 to threads) {
      val l = new RandomAccess();
      l.start()
    }
    
    val start = System.currentTimeMillis()
    runBarrier.await() // go!
    runBarrier.await() // everyone's done
    return System.currentTimeMillis() - start
  } 
  
  def parseArgs(args:Array[String]): Unit = {
      
    var i:Int = 0
    var arg:String = ""
    var opterr:Boolean = false

    while (i < args.length) {
      if (args(i).charAt(0) == '-') { 
        arg = args(i)
        i=i+1
        if (arg.equals("-t")) {
          threads= args(i).toInt
          if (threads <= 0) opterr = true
        } else if (arg.equals("-n")) {
          numElem = args(i).toInt
          if (numElem <= 0) opterr = true
        } else if (arg.equals("-a")) {
          numAccesses = args(i).toInt
          if (numAccesses <= 0) opterr = true
        } else if (arg.equals("-b")) {
          batchSize = args(i).toInt
          if (batchSize <= 0) opterr = true
        } else if (arg.equals("--verbose")) {
          verbose = true
        } else if (arg.equals("-w")) {
          warmups = args(i).toInt
          if (warmups <= 0) opterr = true
        } else if (arg.equals("-r")) {
          runs = args(i).toInt
          if (runs <= 0) opterr = true
        } else if (arg.equals("--verbose")) {
          verbose = true
        } else {
          println("Non-option argument: " + args(i-1));
          opterr = true;
        }
        i = i+1
      } else {
        opterr = true
      }
      
      if (opterr) {
        RandomAccess.displayUsage()
        exit(1)
      }
    }
  }
  
  private def displayUsage(): Unit = {
    println("RandomAccess benchmark usage: RandomAccess [options]")
    println("Options:")
    println("    -t          number of threads (must be > 0)")
    println("    -n          number of elements in the list(must be > 0)") 
    println("    -a          number of accesses (must be > 0)")
    println("    -b          batch size (must be > 0, > 1 to cause deadlock")
    println("    --verbose   yap yap yap")
    println("    -w          number of warmups (must be > 0)")
    println("    -r          number of runs (must be > 0)")
  }
}

class RandomAccess() extends Thread {
  
  override def run():Unit = {

    val id = Thread.currentThread().getId()
    
    // setup
    val l = RandomAccess.getContentionList()
    
    RandomAccess.getRunBarrier().await()
    
    val numAccesses = RandomAccess.numAccesses
    val batchSize = RandomAccess.batchSize
    
    val gen = new Random()
    
    val numBatches = numAccesses/batchSize + 1
    var batch = 1
    var nextBatchSize = batchSize
    
    var acquired = ListBuffer[DummyData]()

    for (a <- 1 to numAccesses by batchSize) {
      
      if (batch == numBatches) nextBatchSize = numAccesses - a + 1
          
      // batch acquire
      for (b <- 1 to nextBatchSize) {
        val e = l(gen.nextInt(RandomAccess.numElem))
        if (AbstractLock.lock(e)) acquired += e // only add to acquired if I don't already own it
        e.getV() + 1
      }
      
      // batch release
      acquired.foreach( (e) => AbstractLock.unlock(e) )
      acquired.clear()      
    }
    
    // random accesses 
//    for (i <- 1 to RandomAccess.numAccesses) {
//      // pick random 
//      val index = gen.nextInt(RandomAccess.numElem)
//      val d = l(index)
//      AbstractLock.lock(d)
//      d.getV() + 1
//      AbstractLock.unlock(d)
//    }
    
    RandomAccess.getRunBarrier().await()
  }
    
}

