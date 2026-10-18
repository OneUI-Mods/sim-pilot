package dev.simpilot

internal data class LogGateDecision(
    val shouldLog: Boolean,
    val suppressedSinceLastEntry: Int = 0,
)

/** Keeps repeated stable-state messages useful without allowing them to bury incident history. */
internal class RepeatedStateLogGate(private val repeatIntervalMs: Long) {
    private var lastObservedKey: String? = null
    private var lastLoggedAt = Long.MIN_VALUE
    private var suppressed = 0

    @Synchronized
    fun evaluate(key: String, now: Long): LogGateDecision {
        val stateChanged = key != lastObservedKey
        val intervalElapsed = elapsedSince(lastLoggedAt, now) >= repeatIntervalMs
        if (stateChanged || intervalElapsed) {
            val decision = LogGateDecision(true, suppressed)
            lastObservedKey = key
            lastLoggedAt = now
            suppressed = 0
            return decision
        }

        suppressed++
        return LogGateDecision(false)
    }
}

/** Rate-limits noisy callback sources independently while retaining their suppression counts. */
internal class PerKeyLogGate(private val repeatIntervalMs: Long) {
    private data class State(var lastLoggedAt: Long, var suppressed: Int)

    private val states = mutableMapOf<String, State>()

    @Synchronized
    fun evaluate(key: String, now: Long): LogGateDecision {
        val state = states[key]
        if (state == null) {
            states[key] = State(now, 0)
            return LogGateDecision(true)
        }
        if (elapsedSince(state.lastLoggedAt, now) >= repeatIntervalMs) {
            val decision = LogGateDecision(true, state.suppressed)
            state.lastLoggedAt = now
            state.suppressed = 0
            return decision
        }

        state.suppressed++
        return LogGateDecision(false)
    }
}

private fun elapsedSince(previous: Long, now: Long): Long =
    if (previous == Long.MIN_VALUE || now < previous) Long.MAX_VALUE else now - previous
