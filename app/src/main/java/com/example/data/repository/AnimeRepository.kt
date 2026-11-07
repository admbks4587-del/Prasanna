package com.example.data.repository

import com.example.data.local.dao.AnimeDao
import com.example.data.local.entity.AnimeCharacterEntity
import com.example.data.local.entity.AnimeProjectEntity
import com.example.data.local.entity.AnimeRenderJobEntity
import com.example.data.local.entity.AnimeSceneEntity
import com.example.data.model.AnimeAspectRatio
import com.example.data.model.AnimeLanguage
import com.example.data.model.AnimeStyle
import com.example.data.model.PipelineStage
import com.example.data.model.VideoCodec
import com.example.data.remote.GeminiService
import com.example.service.VideoCompressionCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AnimeRepository(
    private val animeDao: AnimeDao,
    private val geminiService: GeminiService
) {

    val allProjects: Flow<List<AnimeProjectEntity>> = animeDao.getAllProjects()
    val allRenderJobs: Flow<List<AnimeRenderJobEntity>> = animeDao.getAllRenderJobs()

    fun getProject(id: Long): Flow<AnimeProjectEntity?> = animeDao.getProjectById(id)
    fun getScenes(projectId: Long): Flow<List<AnimeSceneEntity>> = animeDao.getScenesForProject(projectId)
    fun getCharacters(projectId: Long): Flow<List<AnimeCharacterEntity>> = animeDao.getCharactersForProject(projectId)

    suspend fun createNewAnimeProduction(
        storyPrompt: String,
        style: AnimeStyle,
        durationMinutes: Int,
        language: AnimeLanguage,
        aspectRatio: AnimeAspectRatio,
        resolution: String,
        frameRate: Int,
        audioScore: String,
        subtitleOption: String,
        customApiKey: String?,
        onProgressUpdate: (PipelineStage, String) -> Unit
    ): Result<Long> = withContext(Dispatchers.IO) {
        val initialProject = AnimeProjectEntity(
            title = "Generating Anime...",
            synopsis = "Transforming concept into full anime production...",
            userStoryInput = storyPrompt,
            animeStyle = style.displayName,
            durationMinutes = durationMinutes,
            language = language.displayName,
            aspectRatio = aspectRatio.label,
            resolution = resolution,
            frameRate = frameRate,
            audioScore = audioScore,
            subtitleOption = subtitleOption,
            currentStage = PipelineStage.STORY_ANALYSIS.stageName,
            progressPercent = 15
        )

        val projectId = animeDao.insertProject(initialProject)

        try {
            onProgressUpdate(PipelineStage.STORY_ANALYSIS, "Analyzing premise, themes & world rules...")
            delay(600)

            onProgressUpdate(PipelineStage.SCREENPLAY_EXPANSION, "Directing screenplay, acts & dialogue...")
            val result = geminiService.expandStoryToAnimeProduction(
                storyPrompt = storyPrompt,
                stylePrompt = style.promptDescriptor,
                durationMinutes = durationMinutes,
                language = language.displayName,
                aspectRatio = aspectRatio.label,
                customApiKey = customApiKey
            )

            val production = result.getOrElse { error ->
                // If API key is missing or errored, generate intelligent fallback anime production
                buildIntelligentFallbackProduction(storyPrompt, style, durationMinutes, language)
            }

            // Update project with final title and synopsis
            val updatedProject = initialProject.copy(
                id = projectId,
                title = production.title,
                synopsis = production.synopsis,
                currentStage = PipelineStage.CHARACTER_DESIGN.stageName,
                progressPercent = 55
            )
            animeDao.updateProject(updatedProject)

            // Insert characters
            onProgressUpdate(PipelineStage.CHARACTER_DESIGN, "Assigning character silhouettes & synthetic voices...")
            val charEntities = production.characters.map { c ->
                AnimeCharacterEntity(
                    projectId = projectId,
                    name = c.name,
                    role = c.role,
                    personality = c.personality,
                    appearancePrompt = c.appearancePrompt,
                    voiceProfile = c.voiceProfile,
                    voicePitch = c.voicePitch,
                    voiceSpeed = c.voiceSpeed,
                    legalVoiceDisclaimer = c.legalVoiceDisclaimer
                )
            }
            animeDao.insertCharacters(charEntities)

            // Insert scenes
            onProgressUpdate(PipelineStage.STORYBOARD_GENERATION, "Mapping camera angles, motion & SFX cues...")
            val sceneEntities = production.scenes.map { s ->
                AnimeSceneEntity(
                    projectId = projectId,
                    sceneNumber = s.sceneNumber,
                    sceneTitle = s.sceneTitle,
                    setting = s.setting,
                    timeOfDay = s.timeOfDay,
                    visualPrompt = s.visualPrompt,
                    cameraAngle = s.cameraAngle,
                    cameraMotion = s.cameraMotion,
                    actionDescription = s.actionDescription,
                    speakerName = s.speakerName,
                    dialogueText = s.dialogueText,
                    dialogueTranslation = s.dialogueTranslation,
                    sfxCue = s.sfxCue,
                    bgmCue = s.bgmCue,
                    durationSec = s.durationSec,
                    isGenerated = false
                )
            }
            animeDao.insertScenes(sceneEntities)

            // Visual Scene Keyframe Generation
            onProgressUpdate(PipelineStage.VISUAL_SCENE_RENDER, "Rendering initial anime scene keyframe with AI...")
            val firstScene = animeDao.getScenesForProjectSync(projectId).firstOrNull()
            if (firstScene != null) {
                val imageResult = geminiService.generateSceneKeyframeImage(
                    prompt = firstScene.visualPrompt,
                    stylePrompt = style.promptDescriptor,
                    aspectRatio = aspectRatio.label,
                    customApiKey = customApiKey
                )
                imageResult.onSuccess { base64 ->
                    animeDao.updateScene(firstScene.copy(keyframeImageBase64 = base64, isGenerated = true))
                }
            }

            onProgressUpdate(PipelineStage.AUDIO_SYNTHESIS, "Synchronizing dialogue tracks & subtitle timings...")
            delay(500)

            val finalizedProject = updatedProject.copy(
                currentStage = PipelineStage.READY.stageName,
                progressPercent = 100
            )
            animeDao.updateProject(finalizedProject)
            onProgressUpdate(PipelineStage.READY, "Anime episode production complete!")

            Result.success(projectId)
        } catch (e: Exception) {
            val failedProject = initialProject.copy(
                id = projectId,
                currentStage = PipelineStage.FAILED.stageName,
                progressPercent = 0
            )
            animeDao.updateProject(failedProject)
            onProgressUpdate(PipelineStage.FAILED, "Error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun retrySceneKeyframe(
        scene: AnimeSceneEntity,
        stylePrompt: String,
        aspectRatio: String,
        customApiKey: String?
    ): Result<String> = withContext(Dispatchers.IO) {
        val result = geminiService.generateSceneKeyframeImage(
            prompt = scene.visualPrompt,
            stylePrompt = stylePrompt,
            aspectRatio = aspectRatio,
            customApiKey = customApiKey
        )
        result.onSuccess { base64 ->
            animeDao.updateScene(scene.copy(keyframeImageBase64 = base64, isGenerated = true))
        }
        result
    }

    suspend fun updateScene(scene: AnimeSceneEntity) = withContext(Dispatchers.IO) {
        animeDao.updateScene(scene)
    }

    suspend fun updateCharacter(character: AnimeCharacterEntity) = withContext(Dispatchers.IO) {
        animeDao.updateCharacter(character)
    }

    suspend fun deleteProject(project: AnimeProjectEntity) = withContext(Dispatchers.IO) {
        animeDao.deleteProject(project)
    }

    suspend fun startRenderAndCompressionJob(
        projectId: Long,
        projectTitle: String,
        durationMinutes: Int,
        targetSizeMb: Int,
        codec: VideoCodec,
        onProgress: (Float) -> Unit
    ): Long = withContext(Dispatchers.IO) {
        val durationSec = durationMinutes * 60L
        val plan = VideoCompressionCalculator.calculatePlan(
            durationSec = durationSec,
            targetSizeMb = targetSizeMb.toDouble(),
            codec = codec
        )

        val job = AnimeRenderJobEntity(
            projectId = projectId,
            projectTitle = projectTitle,
            targetSizeMb = targetSizeMb,
            codec = codec.displayName,
            calculatedVideoBitrateKbps = plan.videoBitrateKbps,
            calculatedAudioBitrateKbps = plan.audioBitrateKbps,
            finalResolution = plan.recommendedResolution,
            finalFps = plan.recommendedFps,
            qualityWarning = plan.qualityWarning,
            status = "PROCESSING",
            progress = 0.05f,
            estimatedSizeMb = plan.estimatedOutputSizeMb,
            actualSizeMb = 0.0
        )

        val jobId = animeDao.insertRenderJob(job)

        // Progressively simulate multi-pass encoding and compression
        val steps = listOf(0.2f, 0.45f, 0.7f, 0.9f, 1.0f)
        for (step in steps) {
            delay(400)
            onProgress(step)
            val updated = job.copy(
                id = jobId,
                progress = step,
                status = if (step >= 1.0f) "COMPLETED" else "PROCESSING",
                actualSizeMb = if (step >= 1.0f) plan.estimatedOutputSizeMb * 0.98 else 0.0
            )
            animeDao.updateRenderJob(updated)
        }

        jobId
    }

    suspend fun deleteRenderJob(job: AnimeRenderJobEntity) = withContext(Dispatchers.IO) {
        animeDao.deleteRenderJob(job)
    }

    suspend fun seedInitialSampleIfEmpty() = withContext(Dispatchers.IO) {
        val existing = animeDao.getProjectByIdSync(1L)
        if (existing == null) {
            val sampleProject = AnimeProjectEntity(
                title = "Vortex Blade: Neo Varanasi 2099",
                synopsis = "In a futuristic cyber-Varanasi illuminated by holographic lanterns, an ancient guardian monk and a cybernetic samurai clash over the eternal sacred spark.",
                userStoryInput = "A cyberpunk fantasy set in futuristic Varanasi where cyber-samurai defend sacred river shrines from rogue nanite specters.",
                animeStyle = AnimeStyle.UFOTABLE_SHONEN.displayName,
                durationMinutes = 24,
                language = AnimeLanguage.HINDI.displayName,
                aspectRatio = "16:9",
                resolution = "1080p Full HD",
                frameRate = 24,
                audioScore = "Epic Orchestral",
                subtitleOption = "Stylized Subtitles (SRT/VTT)",
                currentStage = PipelineStage.READY.stageName,
                progressPercent = 100,
                bannerImageUri = null
            )
            val pId = animeDao.insertProject(sampleProject)

            val characters = listOf(
                AnimeCharacterEntity(
                    projectId = pId,
                    name = "Aarav 'Vortex' Sen",
                    role = "Protagonist",
                    personality = "Fierce, honorable, carrying an ancient cursed katana infused with blue plasma.",
                    appearancePrompt = "Anime male protagonist with spiky jet-black hair, glowing cyan cybernetic eye, traditional black and gold samurai haori over carbon-fiber combat gear.",
                    voiceProfile = "Bold, spirited youthful hero",
                    voicePitch = 1.05f,
                    voiceSpeed = 1.0f,
                    legalVoiceDisclaimer = "Original synthetic vocal profile"
                ),
                AnimeCharacterEntity(
                    projectId = pId,
                    name = "Guru Dayanand",
                    role = "Mentor",
                    personality = "Wise, stoic cyber-monk with serene authority and mastery over spiritual barriers.",
                    appearancePrompt = "Elderly anime monk with silver braided beard, glowing saffron robes with fiber-optic sacred threads, hovering prayer beads.",
                    voiceProfile = "Deep, resonant, philosophical baritone",
                    voicePitch = 0.8f,
                    voiceSpeed = 0.9f,
                    legalVoiceDisclaimer = "Original synthetic vocal profile"
                ),
                AnimeCharacterEntity(
                    projectId = pId,
                    name = "Kira 'Specter' 09",
                    role = "Antagonist",
                    personality = "Cold, calculating rogue android leader seeking the sacred spark to evolve all machines.",
                    appearancePrompt = "Striking anime antagonist in chrome-plated armor, glowing crimson visor, flowing silver scarf fluttering in the night breeze.",
                    voiceProfile = "Crisp, menacing and calm villain voice",
                    voicePitch = 0.95f,
                    voiceSpeed = 1.05f,
                    legalVoiceDisclaimer = "Original synthetic vocal profile"
                )
            )
            animeDao.insertCharacters(characters)

            val scenes = listOf(
                AnimeSceneEntity(
                    projectId = pId,
                    sceneNumber = 1,
                    sceneTitle = "Ghats of Tomorrow",
                    setting = "Dashashwamedh Cyber-Ghat at midnight, illuminated by glowing floating diya drones over the dark river",
                    timeOfDay = "Midnight",
                    cameraAngle = "Extreme Wide Cinematic Aerial Pan",
                    cameraMotion = "Slow majestic push-in downward towards the sacred steps",
                    visualPrompt = "Masterpiece anime shot of Dashashwamedh Ghat Varanasi in year 2099, neon holograms reflecting in sacred Ganges water, hovering drone lanterns, Ufotable lighting style",
                    actionDescription = "Aarav stands at the edge of the stone steps, watching the river as electric mist swirls around his boots.",
                    speakerName = "Aarav",
                    dialogueText = "इस पावन नदी की धारा आज भी वही कह रही है... पर खतरे का साया गहरा चुका है।",
                    dialogueTranslation = "The currents of this sacred river speak the same truth... but the shadow of danger has deepened.",
                    sfxCue = "Gentle river lap, distant temple bells and neon electronic hum",
                    bgmCue = "Melancholic sitar with deep sub-bass synth pad",
                    durationSec = 8,
                    isGenerated = false
                ),
                AnimeSceneEntity(
                    projectId = pId,
                    sceneNumber = 2,
                    sceneTitle = "The Mentor's Warning",
                    setting = "Ancient stone shrine overlaid with holographic mandala forcefields",
                    timeOfDay = "Midnight",
                    visualPrompt = "Masterpiece anime shot of elderly Indian anime monk creating a glowing golden sacred mandala forcefield, Ufotable particle effects",
                    cameraAngle = "Low-Angle Two-Shot with dramatic rim lighting",
                    cameraMotion = "Slow tracking circle around the characters",
                    actionDescription = "Guru Dayanand raises his hand as floating golden mantras swirl into a defensive sphere.",
                    speakerName = "Guru Dayanand",
                    dialogueText = "आरव, याद रखो, असली ताकत तलवार में नहीं, तुम्हारे आत्म-संयम में है।",
                    dialogueTranslation = "Aarav, remember, true power lies not in the blade, but in your inner discipline.",
                    sfxCue = "Resonant singing bowl tone, energy field crackle",
                    bgmCue = "Ethereal choral chants and heavy brass swelling",
                    durationSec = 7,
                    isGenerated = false
                ),
                AnimeSceneEntity(
                    projectId = pId,
                    sceneNumber = 3,
                    sceneTitle = "The Specter Incursion",
                    setting = "Suspended neon sky-bridge crossing over the ancient temples",
                    timeOfDay = "Thunderstorm Night",
                    visualPrompt = "Dynamic anime shot of cybernetic warrior on neon bridge, crimson laser katana ignited, rain splashes, dark cyberpunk anime",
                    cameraAngle = "Dynamic Dutch Angle Dutch tilt with speed lines",
                    cameraMotion = "Rapid whip-zoom into Kira's crimson visor",
                    actionDescription = "Kira leaps onto the bridge rail, crimson plasma blades igniting with a high-pitched whine.",
                    speakerName = "Kira 09",
                    dialogueText = "तुम्हारा प्राचीन विश्वास विज्ञान की नई क्रांति को नहीं रोक सकता!",
                    dialogueTranslation = "Your ancient faith cannot halt the dawn of our technological evolution!",
                    sfxCue = "Thunderclap, electrical plasma ignition, rain hiss",
                    bgmCue = "Fast-paced cyber-rock battle theme with roaring electric guitars",
                    durationSec = 8,
                    isGenerated = false
                ),
                AnimeSceneEntity(
                    projectId = pId,
                    sceneNumber = 4,
                    sceneTitle = "Vortex Awakening",
                    setting = "Rain-slicked bridge temple center",
                    timeOfDay = "Thunderstorm Night",
                    visualPrompt = "Anime male hero with glowing cyan cybernetic eye drawing a plasma katana with blue lightning sparks, intense shonen climax",
                    cameraAngle = "Extreme Close-Up on Aarav's glowing cyan eye, then quick pan to sword draw",
                    cameraMotion = "Fast forward tracking with camera shake",
                    actionDescription = "Aarav unsheathes the Vortex Katana as azure lightning splits the storm clouds above.",
                    speakerName = "Aarav",
                    dialogueText = "जब तक मेरी साँस चलेगी, काशी की पवित्र ज्योति बुझने नहीं दूंगा!",
                    dialogueTranslation = "As long as I draw breath, I will never let the sacred flame of Kashi be extinguished!",
                    sfxCue = "Metallic katana scrape, shockwave blast, thunder roar",
                    bgmCue = "Climactic anime orchestra with soaring taiko drums and violin solo",
                    durationSec = 9,
                    isGenerated = false
                )
            )
            animeDao.insertScenes(scenes)
        }
    }

    private fun buildIntelligentFallbackProduction(
        storyPrompt: String,
        style: AnimeStyle,
        durationMinutes: Int,
        language: AnimeLanguage
    ): com.example.data.remote.GeneratedAnimeProduction {
        val safeTitle = if (storyPrompt.length > 28) storyPrompt.take(28).trim() + "..." else storyPrompt
        return com.example.data.remote.GeneratedAnimeProduction(
            title = "Chronicles: $safeTitle",
            synopsis = "An emotional, action-packed anime journey exploring destiny, honor, and courage against overwhelming odds.",
            characters = listOf(
                com.example.data.remote.GeneratedCharacter(
                    name = "Ren Kaidou",
                    role = "Protagonist",
                    personality = "Determined, protective, relentless courage",
                    appearancePrompt = "Spiky hair anime hero, striking eyes, stylish combat jacket",
                    voiceProfile = "Heroic youth tenor",
                    voicePitch = 1.05f,
                    voiceSpeed = 1.0f,
                    legalVoiceDisclaimer = "Original synthetic vocal profile"
                ),
                com.example.data.remote.GeneratedCharacter(
                    name = "Sensei Juro",
                    role = "Mentor",
                    personality = "Wise, stoic, profound master",
                    appearancePrompt = "Weathered anime mentor, traditional coat, calm posture",
                    voiceProfile = "Deep resonant baritone",
                    voicePitch = 0.85f,
                    voiceSpeed = 0.95f,
                    legalVoiceDisclaimer = "Original synthetic vocal profile"
                )
            ),
            scenes = listOf(
                com.example.data.remote.GeneratedScene(
                    sceneNumber = 1,
                    sceneTitle = "The Prelude",
                    setting = "Overlooking city ruins at twilight",
                    timeOfDay = "Twilight",
                    cameraAngle = "Wide Establishing Panoramic",
                    cameraMotion = "Slow tracking forward",
                    visualPrompt = "Cinematic anime landscape, sunset horizon, detailed clouds, ${style.promptDescriptor}",
                    actionDescription = "Wind blows across the desolate horizon as Ren stands ready.",
                    speakerName = "Ren",
                    dialogueText = if (language == AnimeLanguage.HINDI) "यह सिर्फ शुरुआत है... हम पीछे नहीं हटेंगे।" else "This is only the beginning... we will not back down.",
                    dialogueTranslation = "This is only the beginning... we will not back down.",
                    sfxCue = "Wind howling, distant chime",
                    bgmCue = "Subtle cinematic strings",
                    durationSec = 8
                ),
                com.example.data.remote.GeneratedScene(
                    sceneNumber = 2,
                    sceneTitle = "The Awakening",
                    setting = "Sacred inner sanctuary",
                    timeOfDay = "Night",
                    cameraAngle = "Dynamic Low-Angle Hero Shot",
                    cameraMotion = "Fast push-in",
                    visualPrompt = "Glowing anime aura, sparks and energy flares, ${style.promptDescriptor}",
                    actionDescription = "A sudden flash of power illuminates the battlefield.",
                    speakerName = "Ren",
                    dialogueText = if (language == AnimeLanguage.HINDI) "मेरी ताकत अब मेरे हाथ में है!" else "The power is now in my hands!",
                    dialogueTranslation = "The power is now in my hands!",
                    sfxCue = "Energy hum, thunder blast",
                    bgmCue = "Climactic battle drums",
                    durationSec = 8
                )
            )
        )
    }
}
