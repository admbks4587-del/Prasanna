package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anime_render_jobs")
data class AnimeRenderJobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val projectTitle: String,
    val targetSizeMb: Int,
    val codec: String,
    val calculatedVideoBitrateKbps: Int,
    val calculatedAudioBitrateKbps: Int,
    val finalResolution: String,
    val finalFps: Int,
    val qualityWarning: String?,
    val status: String, // "QUEUED", "RENDERING", "COMPLETED", "FAILED"
    val progress: Float,
    val estimatedSizeMb: Double,
    val actualSizeMb: Double,
    val exportTimestamp: Long = System.currentTimeMillis()
)
