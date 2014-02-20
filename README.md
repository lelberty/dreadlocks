Dreadlocks Deadlock Detection
=======================================

Overview
--------

This is a Scala implementation of [Dreadlocks](http://www.cs.nyu.edu/~ejk/papers/dreadlocks-spaa08.pdf).

Dreadlocks is a deadlock detection algorithm that uses bit vectors (digests) to represent dependencies. Specifically, each thread T's digest is a bitmap of thread ids, where the value at index n indicates whether or not T is waiting for n. Deadlock is detected when the owner of the lock depends on a thread attempting to acquire the lock.

My Implementation
-----------------

Emphasis is on performance; cache-locality and minimizing synchronization primitives are my two main goals. My implementation, although semantically equivalent, uses a set of dependents rather than a bit vector of dependencies. This allows me to have:

1. Lock-free union of dependent sets
2. O(1), cache-local deadlock check

Union is still affected by the exponential blowup that comes with back-propogation, however I use no synchronization primitives. I use immutable lists of dependents to avoid synchronization blocks. Union guarantees that at least one thread in a cycle will detect deadlock and notify the rest. Note that if the detector thread crashes before notifying, a false negative will occur for all other running threads in the cycle.

Although my dependent sets have lock free union, membership, and clear operations, my ticket distribution system is not lock-free. Some of this can be simplified if we can guarantee only a small number of threads will be active in the system at any given time.

Preliminary benchmarking has been done, but not with a recent version of the algorithm. Future work will be to develop more revealing benchmarks designed with practicality in mind. Designing benchmarks for sytems involving heavy parallelism is not an easy task, but we hope to assemble a series of tests that will demonstrate Dreadlock's efficiency in a practical environment.

Running
-------

Use sbt to run.

I use the ScalaTest concurrency package to do my testing. Tests should be run with 'testOnly dreadlocks.test.SUITENAME'. To run the DreadLock suite:

testOnly dreadlocks.test.DreadLockSpec

Tests should be only run with 'testOnly'. Weird memory bugs occur when all suites are run with 'test'.

IMPORTANT: The DreadLockSpec test that runs a 3-way deadlock MAY FAIL with 'succes is not equal to deadlock'. This failure, unfortuantely, is due to a known bug/limitation in the ScalaTest concurrency package, which is caused by a dash of non-determinism in java.lang.Thread.getState. See the [Conductor documentation](http://doc.scalatest.org/1.4.1/org/scalatest/concurrent/Conductor.html) for details. I have created a test case demonstrating this bug in STBugSpec.

I have two primitive benchmarks: 

* A random access benchmark that causes multiple threads to access and lock random elements in a list. This allows for a high degree of control in tuning deadlock frequency.
* A no deadlock benchmark that creates high contention by locking succesive elements in a list. This is meant to demonstrate the performance overhead of enabling dreadlocks when it is not beneficial to do so.

The Dreadlocks system itself consists of just dreadlocks.core.DreadLock and dreadlocks.core.Ticket.
