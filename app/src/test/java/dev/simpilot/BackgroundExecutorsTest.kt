package dev.simpilot

import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Test

class BackgroundExecutorsTest {
    @Test
    fun queuedWorkStaysSerialAndWorkerReturnsAfterIdle() {
        val worker = BackgroundExecutors.serial("IdleWorkerTest", idleMillis = 20)
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val complete = CountDownLatch(21)
        val order = Collections.synchronizedList(mutableListOf<Int>())
        try {
            worker.execute {
                entered.countDown()
                check(release.await(5, TimeUnit.SECONDS))
                order.add(0)
                complete.countDown()
            }
            assertTrue(entered.await(5, TimeUnit.SECONDS))
            for (i in 1..20) worker.execute {
                order.add(i)
                complete.countDown()
            }
            assertEquals(20, worker.queue.size)
            release.countDown()
            assertTrue(complete.await(5, TimeUnit.SECONDS))
            assertEquals((0..20).toList(), order.toList())
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
            while (worker.poolSize != 0 && System.nanoTime() < deadline) Thread.sleep(5)
            assertEquals(0, worker.poolSize)
            val restarted = worker.submit<Boolean> { true }
            assertTrue(restarted.get(5, TimeUnit.SECONDS))
        } finally {
            release.countDown()
            worker.shutdownNow()
        }
    }
}
