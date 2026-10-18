package dev.simpilot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MonitorLogPolicyTest {
    @Test
    fun repeatedStableStateIsSummarizedAtTheConfiguredInterval() {
        val gate = RepeatedStateLogGate(repeatIntervalMs = 10_000)

        assertTrue(gate.evaluate("wifi-ready", now = 1_000).shouldLog)
        assertFalse(gate.evaluate("wifi-ready", now = 2_000).shouldLog)
        assertFalse(gate.evaluate("wifi-ready", now = 3_000).shouldLog)

        val summary = gate.evaluate("wifi-ready", now = 11_000)
        assertTrue(summary.shouldLog)
        assertEquals(2, summary.suppressedSinceLastEntry)
    }

    @Test
    fun stateChangeIsLoggedImmediately() {
        val gate = RepeatedStateLogGate(repeatIntervalMs = 10_000)

        gate.evaluate("wifi-ready", now = 1_000)
        gate.evaluate("wifi-ready", now = 2_000)
        val changed = gate.evaluate("shizuku-wait", now = 2_100)

        assertTrue(changed.shouldLog)
        assertEquals(1, changed.suppressedSinceLastEntry)
    }

    @Test
    fun callbackSourcesAreRateLimitedIndependently() {
        val gate = PerKeyLogGate(repeatIntervalMs = 10_000)

        assertTrue(gate.evaluate("signal", now = 1_000).shouldLog)
        assertTrue(gate.evaluate("network", now = 1_100).shouldLog)
        assertFalse(gate.evaluate("signal", now = 2_000).shouldLog)

        val signalSummary = gate.evaluate("signal", now = 11_000)
        assertTrue(signalSummary.shouldLog)
        assertEquals(1, signalSummary.suppressedSinceLastEntry)
    }
}
