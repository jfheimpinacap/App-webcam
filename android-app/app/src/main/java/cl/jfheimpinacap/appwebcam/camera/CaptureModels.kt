package cl.jfheimpinacap.appwebcam.camera

data class CaptureSize(val width: Int, val height: Int) {
    override fun toString() = "$width × $height"
}

data class CaptureRequest(val size: CaptureSize = CaptureSize(1280, 720), val fps: Int = 30)

data class UsableCamera(
    val cameraId: String,
    val lens: String,
    val supports60Fps: Boolean,
)

data class CaptureMetrics(
    val frameCount: Long = 0,
    val durationSeconds: Double = 0.0,
    val windowFps: Double? = null,
    val averageFps: Double? = null,
    val minimumWindowFps: Double? = null,
    val maximumWindowFps: Double? = null,
    val observedSize: CaptureSize? = null,
)

enum class SessionPhase { STOPPED, PREPARING, WAITING, ACTIVE, INTERRUPTED, ERROR }
enum class SessionResult { NOT_MEASURED, INSUFFICIENT, MATCHED, RESOLUTION_FALLBACK, FPS_BELOW_TARGET, REJECTED, INTERRUPTED }

object CaptureEvaluation {
    const val MIN_DURATION_SECONDS = 3.0
    const val MIN_FRAMES = 60L
    fun fpsThreshold(target: Int) = if (target >= 60) 54.0 else 27.0

    fun evaluate(request: CaptureRequest, metrics: CaptureMetrics, phase: SessionPhase): SessionResult {
        if (phase == SessionPhase.ERROR) return SessionResult.REJECTED
        if (phase == SessionPhase.INTERRUPTED) return SessionResult.INTERRUPTED
        if (metrics.frameCount < MIN_FRAMES || metrics.durationSeconds < MIN_DURATION_SECONDS || metrics.averageFps == null) {
            return if (metrics.frameCount == 0L) SessionResult.NOT_MEASURED else SessionResult.INSUFFICIENT
        }
        if (metrics.observedSize != request.size) return SessionResult.RESOLUTION_FALLBACK
        return if (metrics.averageFps >= fpsThreshold(request.fps)) SessionResult.MATCHED else SessionResult.FPS_BELOW_TARGET
    }

    fun canExplore60(metadataRanges: List<IntRange>) = metadataRanges.any { it.last >= 60 }
}
