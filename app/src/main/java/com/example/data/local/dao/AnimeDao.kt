package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AnimeCharacterEntity
import com.example.data.local.entity.AnimeProjectEntity
import com.example.data.local.entity.AnimeRenderJobEntity
import com.example.data.local.entity.AnimeSceneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {

    // Projects
    @Query("SELECT * FROM anime_projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<AnimeProjectEntity>>

    @Query("SELECT * FROM anime_projects WHERE id = :id LIMIT 1")
    fun getProjectById(id: Long): Flow<AnimeProjectEntity?>

    @Query("SELECT * FROM anime_projects WHERE id = :id LIMIT 1")
    suspend fun getProjectByIdSync(id: Long): AnimeProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: AnimeProjectEntity): Long

    @Update
    suspend fun updateProject(project: AnimeProjectEntity)

    @Delete
    suspend fun deleteProject(project: AnimeProjectEntity)

    // Scenes
    @Query("SELECT * FROM anime_scenes WHERE projectId = :projectId ORDER BY sceneNumber ASC")
    fun getScenesForProject(projectId: Long): Flow<List<AnimeSceneEntity>>

    @Query("SELECT * FROM anime_scenes WHERE projectId = :projectId ORDER BY sceneNumber ASC")
    suspend fun getScenesForProjectSync(projectId: Long): List<AnimeSceneEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScenes(scenes: List<AnimeSceneEntity>)

    @Update
    suspend fun updateScene(scene: AnimeSceneEntity)

    @Query("DELETE FROM anime_scenes WHERE projectId = :projectId")
    suspend fun deleteScenesForProject(projectId: Long)

    // Characters
    @Query("SELECT * FROM anime_characters WHERE projectId = :projectId ORDER BY id ASC")
    fun getCharactersForProject(projectId: Long): Flow<List<AnimeCharacterEntity>>

    @Query("SELECT * FROM anime_characters WHERE projectId = :projectId ORDER BY id ASC")
    suspend fun getCharactersForProjectSync(projectId: Long): List<AnimeCharacterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<AnimeCharacterEntity>)

    @Update
    suspend fun updateCharacter(character: AnimeCharacterEntity)

    @Query("DELETE FROM anime_characters WHERE projectId = :projectId")
    suspend fun deleteCharactersForProject(projectId: Long)

    // Render Jobs
    @Query("SELECT * FROM anime_render_jobs ORDER BY exportTimestamp DESC")
    fun getAllRenderJobs(): Flow<List<AnimeRenderJobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRenderJob(job: AnimeRenderJobEntity): Long

    @Update
    suspend fun updateRenderJob(job: AnimeRenderJobEntity)

    @Delete
    suspend fun deleteRenderJob(job: AnimeRenderJobEntity)

    @Query("DELETE FROM anime_render_jobs")
    suspend fun clearAllRenderJobs()
}
