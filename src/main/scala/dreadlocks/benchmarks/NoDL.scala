package dreadlocks.benchmarks

import java.util.concurrent.CyclicBarrier
import dreadlocks.core.DreadLock

class DummyData(v:Int) {
  def getV() = v
  override def toString(): String = v.toString()
}

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

    val dl:DreadLock = new DreadLock();
         
     for (i <- 1 to warmups) {
       dl.reset()
       time = runNoDL(dl)
           total += time
       if (verbose) println("Warmup %d completed in: %d"
           .format(i, time))
     }
     
     avg = total/warmups
     
     println("Average warmup time: %d".format(avg))
     avg = 0
     total = 0
     
     for (i <- 1 to runs) {
       dl.reset()
       time = runNoDL(dl)
       total += time
       if (verbose) println("Run %d completed in: %d"
         .format(i, time))
     }
     
     avg = total/runs
     
     println("Average run time: %d".format(avg))
     avg = 0
     total = 0
  }

  private def runNoDL(dl:DreadLock): Long = {
    
    barrier = new CyclicBarrier(threads)
    runBarrier = new CyclicBarrier(threads+1)
    
    // create list

    // spawn processes
    for (i <- 1 to threads) {
      val l = new NoDL(dl);
      (new Thread(l)).start()
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

class NoDL(dl:DreadLock) extends Runnable {
  
  override def run():Unit = {

    val id = Thread.currentThread().getId()
    
    // setup
    val l = NoDL.getContentionList()
    
    NoDL.getRunBarrier().await()
    
    // "process" every element in the list in ascending order
    l.foreach((d) => {
      dl.lock(d)
      d.getV() + 1
      dl.unlock(d)
    })
    
    NoDL.getRunBarrier().await()
  }
    
}

