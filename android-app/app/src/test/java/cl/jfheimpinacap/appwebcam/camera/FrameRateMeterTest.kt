package cl.jfheimpinacap.appwebcam.camera

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FrameRateMeterTest {
    @Test fun calculatesWindowAndSessionAverage() {
        val meter = FrameRateMeter(1_000_000_000)
        var result: CaptureMetrics? = null
        repeat(32) { result = meter.record(1_000_000_000L + it * 33_333_333L, CaptureSize(1280, 720)) ?: result }
        assertEquals(30.0, result!!.windowFps!!, 0.1)
        assertEquals(30.0, result!!.averageFps!!, 0.1)
        assertEquals(32, result!!.frameCount)
    }
    @Test fun ignoresInvalidAndRepeatedTimestamps() {
        val meter = FrameRateMeter(1)
        assertNull(meter.record(0, CaptureSize(1, 1)))
        meter.record(10, CaptureSize(1, 1))
        assertNull(meter.record(10, CaptureSize(1, 1)))
    }
    @Test fun resetStartsASeparateMeasurement() {
        val meter = FrameRateMeter(1)
        meter.record(10, CaptureSize(1, 1)); meter.record(20, CaptureSize(1, 1)); meter.reset()
        assertNull(meter.record(30, CaptureSize(1, 1)))
    }
    @Test fun evaluationRequiresTimeFramesResolutionAndTolerance() {
        val request30 = CaptureRequest(CaptureSize(1280,720), 30)
        assertEquals(SessionResult.INSUFFICIENT, CaptureEvaluation.evaluate(request30, CaptureMetrics(59, 10.0, averageFps = 30.0), SessionPhase.ACTIVE))
        assertEquals(SessionResult.INSUFFICIENT, CaptureEvaluation.evaluate(request30, CaptureMetrics(100, 2.9, averageFps = 30.0), SessionPhase.ACTIVE))
        val stable = CaptureMetrics(100, 4.0, averageFps = 27.0, observedSize = request30.size)
        assertEquals(SessionResult.MATCHED, CaptureEvaluation.evaluate(request30, stable, SessionPhase.ACTIVE))
        assertEquals(SessionResult.FPS_BELOW_TARGET, CaptureEvaluation.evaluate(request30, stable.copy(averageFps = 26.9), SessionPhase.ACTIVE))
        assertEquals(SessionResult.RESOLUTION_FALLBACK, CaptureEvaluation.evaluate(request30, stable.copy(observedSize = CaptureSize(640,480)), SessionPhase.ACTIVE))
        val request60 = request30.copy(fps = 60)
        assertEquals(SessionResult.MATCHED, CaptureEvaluation.evaluate(request60, stable.copy(averageFps = 54.0), SessionPhase.ACTIVE))
        assertEquals(SessionResult.INTERRUPTED, CaptureEvaluation.evaluate(request30, stable, SessionPhase.INTERRUPTED))
        assertTrue(CaptureEvaluation.canExplore60(listOf(15..30, 30..60)))
        assertTrue(!CaptureEvaluation.canExplore60(listOf(15..30)))
    }
}
