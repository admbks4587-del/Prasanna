package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.VideoCodec
import com.example.service.QualityGrade
import com.example.service.VideoCompressionCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AnimeVerse AI", appName)
    }

    @Test
    fun `test video compression calculator for 24 min episode at 40MB`() {
        val durationSec = 24 * 60L // 1440 seconds
        val targetSizeMb = 40.0
        val plan = VideoCompressionCalculator.calculatePlan(
            durationSec = durationSec,
            targetSizeMb = targetSizeMb,
            codec = VideoCodec.HEVC_H265
        )

        // 40MB for 24 min is ~227 kbps total
        assertTrue("Total bitrate should be around 227 kbps", plan.totalBitrateKbps in 200..250)
        assertTrue("Video bitrate should be positive and low", plan.videoBitrateKbps in 100..200)
        assertNotNull("Should produce quality warning for severe compression", plan.qualityWarning)
        assertTrue("Output size estimate should be close to 40 MB", plan.estimatedOutputSizeMb in 38.0..42.0)
    }

    @Test
    fun `test video compression calculator for 5 min short at 250MB`() {
        val durationSec = 5 * 60L // 300 seconds
        val targetSizeMb = 250.0
        val plan = VideoCompressionCalculator.calculatePlan(
            durationSec = durationSec,
            targetSizeMb = targetSizeMb,
            codec = VideoCodec.HEVC_H265
        )

        assertEquals(QualityGrade.STUDIO_PERFECT, plan.qualityGrade)
        assertEquals("1080p (1920x1080)", plan.recommendedResolution)
    }
}
