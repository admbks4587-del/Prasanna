package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "anime_characters",
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
data class AnimeCharacterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val name: String,
    val role: String,
    val personality: String,
    val appearancePrompt: String,
    val voiceProfile: String,
    val voicePitch: Float = 1.0f,
    val voiceSpeed: Float = 1.0f,
    val avatarImageBase64: String? = null,
    val legalVoiceDisclaimer: String? = null
)
