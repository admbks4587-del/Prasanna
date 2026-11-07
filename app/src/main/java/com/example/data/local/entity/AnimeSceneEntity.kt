package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "anime_scenes",
    foreignKeys = [
        ForeignKey(
            entity = AnimeProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class AnimeSceneEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val sceneNumber: Int,
    val sceneTitle: String,
    val setting: String,
    val timeOfDay: String,
    val visualPrompt: String,
    val cameraAngle: String,
    val cameraMotion: String,
    val actionDescription: String,
    val speakerName: String,
    val dialogueText: String,
    val dialogueTranslation: String = "",
    val sfxCue: String = "",
    val bgmCue: String = "",
    val durationSec: Int = 8,
    val keyframeImageBase64: String? = null,
    val keyframeImageUri: String? = null,
    val isGenerated: Boolean = false
)
