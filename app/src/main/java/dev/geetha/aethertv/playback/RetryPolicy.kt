package dev.geetha.aethertv.playback

enum class PlaybackFault {
    BufferUnderrun,
    Network,
    Source,
}

data class RetryDecision(
    val retry: Boolean,
    val attempt: Int,
    val delayMs: Long,
)

class RetryPolicy(private val maxAttempts: Int = 3) {
    fun decide(attempt: Int, fault: PlaybackFault): RetryDecision {
        if (fault == PlaybackFault.Source) {
            return RetryDecision(retry = false, attempt = attempt, delayMs = 0)
        }
        val next = attempt + 1
        if (next > maxAttempts) {
            return RetryDecision(retry = false, attempt = attempt, delayMs = 0)
        }
        val delay = when (fault) {
            PlaybackFault.BufferUnderrun -> 400L * next
            PlaybackFault.Network -> 800L * next
            PlaybackFault.Source -> 0L
        }
        return RetryDecision(retry = true, attempt = next, delayMs = delay)
    }
}

class PlaybackController(private val policy: RetryPolicy = RetryPolicy()) {
    var attempt: Int = 0
        private set
    var buffering: Boolean = false
        private set
    var stopped: Boolean = false
        private set

    fun onBuffering() {
        buffering = true
    }

    fun onReady() {
        buffering = false
        stopped = false
    }

    fun onFault(fault: PlaybackFault): RetryDecision {
        val decision = policy.decide(attempt, fault)
        if (decision.retry) {
            attempt = decision.attempt
            buffering = true
            stopped = false
        } else {
            stopped = true
            buffering = false
        }
        return decision
    }
}
