package com.example.service

import com.example.data.model.VideoCodec
import kotlin.math.roundToInt

data class CompressionPlan(
    val targetSizeMb: Double,
    val durationSec: Long,
    val codec: VideoCodec,
    val totalBitrateKbps: Int,
    val videoBitrateKbps: Int,
    val audioBitrateKbps: Int,
    val recommendedResolution: String,
    val recommendedFps: Int,
    val estimatedOutputSizeMb: Double,
    val qualityWarning: String?,
    val qualityGrade: QualityGrade
)

enum class QualityGrade(val label: String, val isSevere: Boolean) {
    STUDIO_PERFECT("Studio Master (Flawless)", false),
    HIGH_DEFINITION("Crisp Anime HD (Recommended)", false),
    ACCEPTABLE_MOBILE("Compact Mobile (Minor Softness)", false),
    HEAVY_DEGRADATION("Severe Compression Loss (High Artifacts)", true),
    EXTREME_LOSS("Extreme Quality Loss (Heavy Pixelation)", true)
}

object VideoCompressionCalculator {

    fun calculatePlan(
        durationSec: Long,
        targetSizeMb: Double,
        codec: VideoCodec
    ): CompressionPlan {
        val safeDuration = maxOf(10L, durationSec)
        val safeTargetMb = maxOf(5.0, targetSizeMb)

        // Total bits available in kbps: (MB * 8192) / seconds
        val totalBitrateKbps = ((safeTargetMb * 8192.0) / safeDuration).roundToInt()

        // Allocate audio bitrate smartly
        val audioBitrateKbps = when {
            totalBitrateKbps > 1800 -> 192
            totalBitrateKbps > 800 -> 128
            totalBitrateKbps > 350 -> 96
            totalBitrateKbps > 150 -> 64
            else -> 48
        }

        val rawVideoBitrateKbps = maxOf(32, totalBitrateKbps - audioBitrateKbps)
        // With HEVC efficiency bonus (~1.5x equivalent perceptual quality)
        val effectiveBitrate = (rawVideoBitrateKbps * codec.efficiencyBonus).roundToInt()

        val (resolution, fps, grade, warning) = when {
            effectiveBitrate >= 3500 -> {
                Quadruple(
                    "1080p (1920x1080)",
                    24,
                    QualityGrade.STUDIO_PERFECT,
                    null
                )
            }
            effectiveBitrate >= 1500 -> {
                Quadruple(
                    "1080p (1920x1080)",
                    24,
                    QualityGrade.HIGH_DEFINITION,
                    null
                )
            }
            effectiveBitrate >= 700 -> {
                Quadruple(
                    "720p (1280x720)",
                    24,
                    QualityGrade.ACCEPTABLE_MOBILE,
                    "Minor softening in dark gradients and fast anime action scenes."
                )
            }
            effectiveBitrate >= 280 -> {
                val durationMin = safeDuration / 60
                Quadruple(
                    "480p (854x480)",
                    20,
                    QualityGrade.HEAVY_DEGRADATION,
                    "WARNING: Compressing ${durationMin}m anime into ${safeTargetMb.toInt()}MB forces low bitrate (${rawVideoBitrateKbps} kbps). Macroblocking and line blur will occur."
                )
            }
            else -> {
                val durationMin = safeDuration / 60
                Quadruple(
                    "360p (640x360)",
                    18,
                    QualityGrade.EXTREME_LOSS,
                    "CRITICAL WARNING: Compressing ${durationMin}m anime into ${safeTargetMb.toInt()}MB yields only ${rawVideoBitrateKbps} kbps. Severe pixelation and high artifacting expected!"
                )
            }
        }

        // Real estimated file size with 2% container muxing overhead
        val estimatedMb = ((rawVideoBitrateKbps + audioBitrateKbps) * safeDuration / 8192.0) * 1.02

        return CompressionPlan(
            targetSizeMb = safeTargetMb,
            durationSec = safeDuration,
            codec = codec,
            totalBitrateKbps = totalBitrateKbps,
            videoBitrateKbps = rawVideoBitrateKbps,
            audioBitrateKbps = audioBitrateKbps,
            recommendedResolution = resolution,
            recommendedFps = fps,
            estimatedOutputSizeMb = (estimatedMb * 10).roundToInt() / 10.0,
            qualityWarning = warning,
            qualityGrade = grade
        )
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
