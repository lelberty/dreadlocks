package dreadlocks.benchmarks

import java.util.concurrent.CyclicBarrier
import dreadlocks.core.DreadLock
import dreadlocks.core.AbstractLock

class DummyData(v:Int) {
  def getV() = v
  override def toString(): String = v.toString()
}

// true t2 n1000000 w10000 r100  | 175  10.0/13.0%   seems to be slower
// false t2 n1000000 w10000 r100 | 155

// true t3 n1000000 w10000 r100  | 238  15.33/24.0%
// false t3 n1000000 w10000 r100 | 192

// true t4 n1000000 w10000 r100  | 257  10.25/19.0%
// false t4 n1000000 w10000 r100 | 216

// true t7 n1000000 w10000 r100  | 481  11.1/19.3%   slightly better
// false t7 n1000000 w6000 r100  | 403

object NoDL {
  
  // Parameters
  var threads:Int = 2
  var warmups:Int = 2000
  var runs:Int = 100
  var numElem:Int = 100
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
       time = runNoDL()
           total += time
       if (verbose) println("Warmup %d completed in: %d"
           .format(i, time))
     }
     
     avg = total/warmups
     
     println("Average warmup time: %d".format(avg))
     avg = 0
     total = 0
     
     for (i <- 1 to runs) {
       time = runNoDL()
       total += time
       if (verbose) println("Run %d completed in: %d"
         .format(i, time))
     }
     
     avg = total/runs
     
     println("Average run time: %d".format(avg))
     avg = 0
     total = 0
  }

  private def runNoDL(): Long = {
    
    barrier = new CyclicBarrier(threads)
    runBarrier = new CyclicBarrier(threads+1)
    
    // create list

    // spawn processes
    for (i <- 1 to threads) {
      val l = new NoDL();
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
        NoDL.displayUsage()
        exit(1)
      }
    }
  }
  
  private def displayUsage(): Unit = {
    println("NoDL benchmark usage: NoDL [options]")
    println("Options:")
    println("    -t          number of threads (must be > 0)")
    println("    --verbose   yap yap yap")
    println("    -w          number of warmups (must be > 0)")
    println("    -r          number of runs (must be > 0)")
  }
}

class NoDL() extends Thread {
  
  override def run():Unit = {

    val id = Thread.currentThread().getId()
    
    // setup
    val l = NoDL.getContentionList()
    
    NoDL.getRunBarrier().await()
    
    // "process" every element in the list in ascending order
    l.foreach((d) => {
      AbstractLock.lock(d)
      d.getV() + 1
      AbstractLock.unlock(d)
    })
    
    NoDL.getRunBarrier().await()
  }
    
}

