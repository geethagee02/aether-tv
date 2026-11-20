package dev.geetha.aethertv.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackTest {
    @Test
    fun catalogCoversHlsDashAndMp4() {
        assertEquals(setOf(StreamKind.HLS, StreamKind.DASH, StreamKind.MP4), SampleCatalog.kinds())
    }

    @Test
    fun bufferUnderrunRetriesWithBackoff() {
        val policy = RetryPolicy(maxAttempts = 3)
        val first = policy.decide(0, PlaybackFault.BufferUnderrun)
        assertTrue(first.retry)
        assertEquals(1, first.attempt)
        assertEquals(400L, first.delayMs)
    }

    @Test
    fun networkRetryStopsAfterMaxAttempts() {
        val policy = RetryPolicy(maxAttempts = 2)
        assertTrue(policy.decide(0, PlaybackFault.Network).retry)
        assertTrue(policy.decide(1, PlaybackFault.Network).retry)
        assertFalse(policy.decide(2, PlaybackFault.Network).retry)
    }

    @Test
    fun sourceFaultDoesNotRetry() {
        val decision = RetryPolicy().decide(0, PlaybackFault.Source)
        assertFalse(decision.retry)
        assertEquals(0, decision.delayMs)
    }

    @Test
    fun controllerRecordsAttemptWhileBuffering() {
        val controller = PlaybackController(RetryPolicy(maxAttempts = 3))
        controller.onBuffering()
        assertTrue(controller.buffering)
        val decision = controller.onFault(PlaybackFault.Network)
        assertTrue(decision.retry)
        assertEquals(1, controller.attempt)
        assertTrue(controller.buffering)
        controller.onReady()
        assertFalse(controller.buffering)
    }
}
