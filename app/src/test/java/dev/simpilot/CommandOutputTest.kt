package dev.simpilot

import java.io.ByteArrayInputStream
import java.io.IOException
import org.junit.Assert.*
import org.junit.Test

class CommandOutputTest {
    @Test
    fun largeOutputIsDrainedBeforeWaitingForExit() {
        val process =
            ProcessBuilder("sh", "-c", "dd if=/dev/zero bs=65536 count=2 2>/dev/null").start()
        assertEquals(131072, CommandOutput.collect(process, 5000, 200000).text.length)
    }

    @Test
    fun bothPipesAreDrainedWithoutADeadlock() {
        val process =
            ProcessBuilder(
                    "sh",
                    "-c",
                    "dd if=/dev/zero bs=65536 count=2 2>/dev/null; dd if=/dev/zero bs=65536 count=2 >&2 2>/dev/null",
                )
                .start()
        assertEquals(262145, CommandOutput.collect(process, 5000, 300000, true).text.length)
    }

    @Test
    fun inputIsPumpedWhileOutputIsConsumed() {
        val bytes = ByteArray(150000) { 65 }
        val process = ProcessBuilder("sh", "-c", "cat").start()
        assertEquals(
            String(bytes),
            CommandOutput.collect(process, 5000, 200000, true, ByteArrayInputStream(bytes)).text,
        )
    }

    @Test
    fun aHungCommandIsTerminated() {
        val process = ProcessBuilder("sh", "-c", "exec sleep 5").start()
        try {
            CommandOutput.collect(process, 100, 1024)
            fail("Timeout required")
        } catch (_: IOException) {}
        assertTrue(process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS))
    }

    @Test
    fun anOversizedResponseIsRejectedRatherThanPartiallyParsed() {
        val process =
            ProcessBuilder("sh", "-c", "dd if=/dev/zero bs=65536 count=2 2>/dev/null").start()
        try {
            CommandOutput.collect(process, 5000, 1024)
            fail("Bound required")
        } catch (_: IOException) {}
        assertTrue(process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS))
    }

    @Test
    fun cancellationPreservesTheInterruptAndStopsTheChild() {
        val process = ProcessBuilder("sh", "-c", "exec sleep 5").start()
        Thread.currentThread().interrupt()
        try {
            CommandOutput.collect(process, 5000, 1024)
            fail("Interrupt required")
        } catch (_: InterruptedException) {
            assertTrue(Thread.currentThread().isInterrupted)
        } finally {
            Thread.interrupted()
        }
        assertTrue(process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS))
    }
}
