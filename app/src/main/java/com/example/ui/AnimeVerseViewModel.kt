package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AnimeCharacterEntity
import com.example.data.local.entity.AnimeProjectEntity
import com.example.data.local.entity.AnimeRenderJobEntity
import com.example.data.local.entity.AnimeSceneEntity
import com.example.data.model.AnimeAspectRatio
import com.example.data.model.AnimeFrameRate
import com.example.data.model.AnimeLanguage
import com.example.data.model.AnimeResolution
import com.example.data.model.AnimeStyle
import com.example.data.model.AudioScoring
import com.example.data.model.CompressionTargetSize
import com.example.data.model.PipelineStage
import com.example.data.model.ProjectDuration
import com.example.data.model.SubtitleOption
import com.example.data.model.VideoCodec
import com.example.data.remote.GeminiService
import com.example.data.repository.AnimeRepository
import com.example.service.AudioVoiceEngine
import com.example.service.CompressionPlan
import com.example.service.VideoCompressionCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavTab(val label: String) {
    STUDIO("Studio"),
    STORYBOARD("Storyboard"),
    CHARACTERS("Characters"),
    THEATER("Theater Player"),
    COMPRESSION("Render & Size"),
    SETTINGS("Settings")
}

data class GenerationUiState(
    val isGenerating: Boolean = false,
    val stage: PipelineStage = PipelineStage.IDLE,
    val detailMessage: String = "",
    val error: String? = null
)

class AnimeVerseViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val geminiService = GeminiService()
    private val repository = AnimeRepository(db.animeDao(), geminiService)
    val voiceEngine = AudioVoiceEngine(application)

    // Navigation
    private val _currentTab = MutableStateFlow(AppNavTab.STUDIO)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    // Studio Form inputs
    val storyInput = MutableStateFlow("")
    val selectedStyle = MutableStateFlow(AnimeStyle.UFOTABLE_SHONEN)
    val selectedDuration = MutableStateFlow(ProjectDuration.TWENTY_FOUR_MIN)
    val selectedLanguage = MutableStateFlow(AnimeLanguage.HINDI)
    val selectedAspectRatio = MutableStateFlow(AnimeAspectRatio.WIDE_16_9)
    val selectedResolution = MutableStateFlow(AnimeResolution.FHD_1080P)
    val selectedFrameRate = MutableStateFlow(AnimeFrameRate.FPS_24)
    val selectedAudioScore = MutableStateFlow(AudioScoring.ORCHESTRAL_SHONEN)
    val selectedSubtitle = MutableStateFlow(SubtitleOption.SOFT_SRT)

    // Generation Pipeline State
    private val _generationState = MutableStateFlow(GenerationUiState())
    val generationState: StateFlow<GenerationUiState> = _generationState.asStateFlow()

    // Selected Project & Lists
    val allProjects: StateFlow<List<AnimeProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRenderJobs: StateFlow<List<AnimeRenderJobEntity>> = repository.allRenderJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedProjectId = MutableStateFlow<Long?>(null)
    val selectedProjectId: StateFlow<Long?> = _selectedProjectId.asStateFlow()

    private val _currentScenes = MutableStateFlow<List<AnimeSceneEntity>>(emptyList())
    val currentScenes: StateFlow<List<AnimeSceneEntity>> = _currentScenes.asStateFlow()

    private val _currentCharacters = MutableStateFlow<List<AnimeCharacterEntity>>(emptyList())
    val currentCharacters: StateFlow<List<AnimeCharacterEntity>> = _currentCharacters.asStateFlow()

    // Compression / Export State
    val selectedTargetSizeOption = MutableStateFlow(CompressionTargetSize.SIZE_40_MB)
    val customTargetSizeMb = MutableStateFlow("40")
    val selectedCodec = MutableStateFlow(VideoCodec.HEVC_H265)
    val isRenderingJob = MutableStateFlow(false)
    val renderProgress = MutableStateFlow(0f)

    // Theater Player State
    val activeSceneIndex = MutableStateFlow(0)
    val isPlaying = MutableStateFlow(false)
    val showSubtitles = MutableStateFlow(true)

    // Settings API Key
    val customApiKey = MutableStateFlow("")

    init {
        viewModelScope.launch {
            repository.seedInitialSampleIfEmpty()
            // Observe projects and select the first if none selected
            allProjects.collect { projects ->
                if (_selectedProjectId.value == null && projects.isNotEmpty()) {
                    selectProject(projects.first().id)
                }
            }
        }
    }

    fun selectTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun selectProject(projectId: Long) {
        _selectedProjectId.value = projectId
        viewModelScope.launch {
            repository.getScenes(projectId).collect { scenes ->
                _currentScenes.value = scenes
            }
        }
        viewModelScope.launch {
            repository.getCharacters(projectId).collect { chars ->
                _currentCharacters.value = chars
            }
        }
    }

    fun populateStoryExample(examplePrompt: String) {
        storyInput.value = examplePrompt
    }

    fun startAnimeGeneration() {
        val input = storyInput.value.trim()
        if (input.isEmpty()) return

        viewModelScope.launch {
            _generationState.value = GenerationUiState(
                isGenerating = true,
                stage = PipelineStage.STORY_ANALYSIS,
                detailMessage = "Initiating automated anime production..."
            )

            val result = repository.createNewAnimeProduction(
                storyPrompt = input,
                style = selectedStyle.value,
                durationMinutes = (selectedDuration.value.durationSec / 60).toInt(),
                language = selectedLanguage.value,
                aspectRatio = selectedAspectRatio.value,
                resolution = selectedResolution.value.displayName,
                frameRate = selectedFrameRate.value.fps,
                audioScore = selectedAudioScore.value.displayName,
                subtitleOption = selectedSubtitle.value.displayName,
                customApiKey = customApiKey.value.takeIf { it.isNotBlank() },
                onProgressUpdate = { stage, message ->
                    _generationState.value = GenerationUiState(
                        isGenerating = stage != PipelineStage.READY && stage != PipelineStage.FAILED,
                        stage = stage,
                        detailMessage = message,
                        error = if (stage == PipelineStage.FAILED) message else null
                    )
                }
            )

            result.onSuccess { projectId ->
                selectProject(projectId)
                _generationState.value = GenerationUiState(
                    isGenerating = false,
                    stage = PipelineStage.READY,
                    detailMessage = "Production complete! Ready in Storyboard & Theater."
                )
                // Switch to storyboard or theater to inspect
                _currentTab.value = AppNavTab.STORYBOARD
            }.onFailure { err ->
                _generationState.value = GenerationUiState(
                    isGenerating = false,
                    stage = PipelineStage.FAILED,
                    error = err.message ?: "Generation error"
                )
            }
        }
    }

    fun retrySceneKeyframe(scene: AnimeSceneEntity) {
        viewModelScope.launch {
            val stylePrompt = selectedStyle.value.promptDescriptor
            val ratio = selectedAspectRatio.value.label
            repository.retrySceneKeyframe(scene, stylePrompt, ratio, customApiKey.value.takeIf { it.isNotBlank() })
        }
    }

    fun updateScene(scene: AnimeSceneEntity) {
        viewModelScope.launch {
            repository.updateScene(scene)
        }
    }

    fun updateCharacter(character: AnimeCharacterEntity) {
        viewModelScope.launch {
            repository.updateCharacter(character)
        }
    }

    fun auditionCharacterVoice(character: AnimeCharacterEntity, sampleText: String) {
        val langCode = selectedLanguage.value.langCode
        voiceEngine.speakDialogue(
            text = sampleText,
            languageCode = langCode,
            pitch = character.voicePitch,
            speed = character.voiceSpeed
        )
    }

    fun playActiveScene() {
        val scenes = _currentScenes.value
        val index = activeSceneIndex.value
        if (index in scenes.indices) {
            val scene = scenes[index]
            isPlaying.value = true
            // Play SFX cue
            viewModelScope.launch {
                voiceEngine.playCinematicCue(scene.sfxCue)
            }
            // Speak dialogue
            voiceEngine.speakDialogue(
                text = scene.dialogueText,
                languageCode = selectedLanguage.value.langCode,
                pitch = 1.0f,
                speed = 1.0f,
                onDone = {
                    isPlaying.value = false
                }
            )
        }
    }

    fun stopPlayback() {
        isPlaying.value = false
        voiceEngine.stop()
    }

    fun nextScene() {
        val scenes = _currentScenes.value
        if (activeSceneIndex.value < scenes.size - 1) {
            activeSceneIndex.value += 1
            stopPlayback()
        }
    }

    fun previousScene() {
        if (activeSceneIndex.value > 0) {
            activeSceneIndex.value -= 1
            stopPlayback()
        }
    }

    fun calculateCurrentCompressionPlan(): CompressionPlan {
        val durationMinutes = selectedDuration.value.durationSec / 60
        val durationSec = selectedDuration.value.durationSec
        val targetSize = if (selectedTargetSizeOption.value == CompressionTargetSize.CUSTOM) {
            customTargetSizeMb.value.toDoubleOrNull() ?: 40.0
        } else {
            selectedTargetSizeOption.value.targetMb.toDouble()
        }

        return VideoCompressionCalculator.calculatePlan(
            durationSec = durationSec,
            targetSizeMb = targetSize,
            codec = selectedCodec.value
        )
    }

    fun startCompressionRenderJob() {
        val projectId = _selectedProjectId.value ?: return
        val durationMin = (selectedDuration.value.durationSec / 60).toInt()
        val targetMb = if (selectedTargetSizeOption.value == CompressionTargetSize.CUSTOM) {
            customTargetSizeMb.value.toIntOrNull() ?: 40
        } else {
            selectedTargetSizeOption.value.targetMb
        }

        viewModelScope.launch {
            isRenderingJob.value = true
            renderProgress.value = 0.05f
            val project = db.animeDao().getProjectByIdSync(projectId)
            val title = project?.title ?: "Anime Episode"

            repository.startRenderAndCompressionJob(
                projectId = projectId,
                projectTitle = title,
                durationMinutes = durationMin,
                targetSizeMb = targetMb,
                codec = selectedCodec.value,
                onProgress = { p ->
                    renderProgress.value = p
                }
            )
            isRenderingJob.value = false
        }
    }

    fun deleteRenderJob(job: AnimeRenderJobEntity) {
        viewModelScope.launch {
            repository.deleteRenderJob(job)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.shutdown()
    }
}
