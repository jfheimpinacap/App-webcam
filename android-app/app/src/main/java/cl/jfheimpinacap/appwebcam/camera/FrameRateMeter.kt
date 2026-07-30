package cl.jfheimpinacap.appwebcam.camera

class FrameRateMeter(private val publishIntervalNanos: Long = 1_000_000_000L) {
    private var first = 0L
    private var last = 0L
    private var windowStart = 0L
    private var frames = 0L
    private var windowFrames = 0L
    private val windows = mutableListOf<Double>()

    fun reset() { first = 0; last = 0; windowStart = 0; frames = 0; windowFrames = 0; windows.clear() }

    fun record(timestampNanos: Long, size: CaptureSize): CaptureMetrics? {
        if (timestampNanos <= 0 || (last != 0L && timestampNanos <= last)) return null
        if (first == 0L) { first = timestampNanos; windowStart = timestampNanos }
        last = timestampNanos; frames++; windowFrames++
        val windowDuration = timestampNanos - windowStart
        if (windowDuration < publishIntervalNanos) return null
        val fps = if (windowFrames > 1) (windowFrames - 1) * 1_000_000_000.0 / windowDuration else 0.0
        windows += fps
        val duration = (last - first) / 1_000_000_000.0
        val average = if (frames > 1 && duration > 0) (frames - 1) / duration else null
        windowStart = timestampNanos; windowFrames = 1
        return CaptureMetrics(frames, duration, fps, average, windows.minOrNull(), windows.maxOrNull(), size)
    }
}
