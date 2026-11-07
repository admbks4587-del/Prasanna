package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anime_projects")
data class AnimeProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val synopsis: String,
    val userStoryInput: String,
    val animeStyle: String,
    val durationMinutes: Int,
    val language: String,
    val aspectRatio: String,
    val resolution: String,
    val frameRate: Int,
    val audioScore: String,
    val subtitleOption: String,
    val currentStage: String,
    val progressPercent: Int,
    val bannerImageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
