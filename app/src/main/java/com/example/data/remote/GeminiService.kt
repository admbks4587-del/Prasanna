package com.example.data.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class GeneratedAnimeProduction(
    val title: String,
    val synopsis: String,
    val characters: List<GeneratedCharacter>,
    val scenes: List<GeneratedScene>
)

data class GeneratedCharacter(
    val name: String,
    val role: String,
    val personality: String,
    val appearancePrompt: String,
    val voiceProfile: String,
    val voicePitch: Float = 1.0f,
    val voiceSpeed: Float = 1.0f,
    val legalVoiceDisclaimer: String? = null
)

data class GeneratedScene(
    val sceneNumber: Int,
    val sceneTitle: String,
    val setting: String,
    val timeOfDay: String,
    val cameraAngle: String,
    val cameraMotion: String,
    val visualPrompt: String,
    val actionDescription: String,
    val speakerName: String,
    val dialogueText: String,
    val dialogueTranslation: String,
    val sfxCue: String,
    val bgmCue: String,
    val durationSec: Int = 8
)

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun getApiKey(customKey: String?): String {
        return if (!customKey.isNullOrBlank()) {
            customKey
        } else {
            try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                ""
            }
        }
    }

    suspend fun expandStoryToAnimeProduction(
        storyPrompt: String,
        stylePrompt: String,
        durationMinutes: Int,
        language: String,
        aspectRatio: String,
        customApiKey: String? = null
    ): Result<GeneratedAnimeProduction> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(customApiKey)
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please set your key in the Secrets panel or in App Settings.")
            )
        }

        val sceneCount = when {
            durationMinutes <= 5 -> 4
            durationMinutes <= 15 -> 6
            durationMinutes <= 24 -> 8
            durationMinutes <= 45 -> 10
            else -> 12
        }

        val promptText = """
            You are a master anime director, showrunner, and screenwriter.
            Transform this story concept into a high-production anime screenplay and visual storyboard.
            
            STORY CONCEPT:
            $storyPrompt
            
            SPECIFICATIONS:
            - Target Duration: $durationMinutes minutes
            - Anime Visual Style: $stylePrompt
            - Primary Dialogue Language: $language (IMPORTANT: All dialogue lines MUST be in $language; for Hindi write natural Hindi dialogue in Devanagari script, for Marathi write natural Marathi dialogue in Devanagari script, for English write English, for Japanese write Japanese/Romaji).
            - Aspect Ratio: $aspectRatio
            - Target Scene Count: $sceneCount detailed scenes covering Act 1 (Hook/Inciting Incident), Act 2 (Rising Action & Climax), and Act 3 (Resolution/Cliffhanger).
            
            LEGAL VOICE CASTING RULES:
            - Assign distinct synthetic voice vocal characteristics (pitch, speed, archetype) to each character.
            - If familiar characters (e.g., Doraemon, Gojo Satoru, Goku, Naruto) appear, you must explicitly note in legalVoiceDisclaimer: "Original synthetic voice inspired by non-identifying vocal traits; does not use original copyrighted voice actor likeness or identity."
            
            OUTPUT REQUIREMENT:
            Respond ONLY with a valid, raw JSON object (no markdown code blocks, no backticks, no trailing explanation).
            Use this exact schema:
            {
              "title": "Anime Title",
              "synopsis": "Engaging 2-sentence synopsis",
              "characters": [
                {
                  "name": "Character Name",
                  "role": "Protagonist / Mentor / Rival / Antagonist / Mascot",
                  "personality": "Key traits",
                  "appearancePrompt": "Detailed visual description including hair color, eyes, outfit, and anime silhouette",
                  "voiceProfile": "Energetic Shonen hero / Deep mysterious mentor / Melodic female lead / etc.",
                  "voicePitch": 1.0,
                  "voiceSpeed": 1.0,
                  "legalVoiceDisclaimer": "Original synthetic vocal profile"
                }
              ],
              "scenes": [
                {
                  "sceneNumber": 1,
                  "sceneTitle": "Scene Title",
                  "setting": "Detailed location",
                  "timeOfDay": "Dusk / Midnight / Dawn / Golden Hour",
                  "cameraAngle": "Extreme Close-Up on eyes / Low-Angle Hero Shot / Wide Cinematic Pan",
                  "cameraMotion": "Slow push-in / Whip pan / Slow tracking tilt",
                  "visualPrompt": "Ultra detailed prompt for AI image generator in the specified anime style depicting the key moment of this scene",
                  "actionDescription": "What happens visually on screen",
                  "speakerName": "Speaking character name (or Narrator)",
                  "dialogueText": "Dialogue line in $language",
                  "dialogueTranslation": "English translation of dialogue",
                  "sfxCue": "Katana unsheathing / Distant thunder / Neon buzz",
                  "bgmCue": "Climactic orchestral strings / Rainy piano theme",
                  "durationSec": 8
                }
              ]
            }
        """.trimIndent()

        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().put("text", promptText))
                    }
                    put("parts", partsArray)
                })
            }
            put("contents", contentsArray)
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.7)
            })
        }

        val request = Request.Builder()
            .url(endpoint)
            .post(requestJson.toString().toRequestBody(jsonMediaType))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val errorMsg = try {
                        val errObj = JSONObject(body).getJSONObject("error")
                        errObj.getString("message")
                    } catch (e: Exception) {
                        "HTTP ${response.code}: ${response.message}"
                    }
                    return@withContext Result.failure(Exception("Gemini API Error: $errorMsg"))
                }

                val responseJson = JSONObject(body)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    return@withContext Result.failure(Exception("No generation output from Gemini."))
                }

                val content = candidates.getJSONObject(0).getJSONObject("content")
                val parts = content.getJSONArray("parts")
                val rawText = parts.getJSONObject(0).getString("text").trim()

                // Clean potential markdown wrap
                val cleanedJson = rawText
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val parsed = JSONObject(cleanedJson)
                val title = parsed.optString("title", "Untitled Anime Production")
                val synopsis = parsed.optString("synopsis", "An epic anime saga unfolds.")

                val charList = mutableListOf<GeneratedCharacter>()
                val charactersJson = parsed.optJSONArray("characters")
                if (charactersJson != null) {
                    for (i in 0 until charactersJson.length()) {
                        val c = charactersJson.getJSONObject(i)
                        charList.add(
                            GeneratedCharacter(
                                name = c.optString("name", "Character ${i + 1}"),
                                role = c.optString("role", "Protagonist"),
                                personality = c.optString("personality", "Determined"),
                                appearancePrompt = c.optString("appearancePrompt", "Anime character design"),
                                voiceProfile = c.optString("voiceProfile", "Expressive synthetic voice"),
                                voicePitch = c.optDouble("voicePitch", 1.0).toFloat(),
                                voiceSpeed = c.optDouble("voiceSpeed", 1.0).toFloat(),
                                legalVoiceDisclaimer = if (c.has("legalVoiceDisclaimer")) c.getString("legalVoiceDisclaimer") else null
                            )
                        )
                    }
                }

                val sceneList = mutableListOf<GeneratedScene>()
                val scenesJson = parsed.optJSONArray("scenes")
                if (scenesJson != null) {
                    for (i in 0 until scenesJson.length()) {
                        val s = scenesJson.getJSONObject(i)
                        sceneList.add(
                            GeneratedScene(
                                sceneNumber = s.optInt("sceneNumber", i + 1),
                                sceneTitle = s.optString("sceneTitle", "Scene ${i + 1}"),
                                setting = s.optString("setting", "Neo City"),
                                timeOfDay = s.optString("timeOfDay", "Night"),
                                cameraAngle = s.optString("cameraAngle", "Cinematic Eye-level"),
                                cameraMotion = s.optString("cameraMotion", "Slow push-in"),
                                visualPrompt = s.optString("visualPrompt", "masterpiece anime scene"),
                                actionDescription = s.optString("actionDescription", "Dramatic anime action occurs"),
                                speakerName = s.optString("speakerName", "Narrator"),
                                dialogueText = s.optString("dialogueText", "..."),
                                dialogueTranslation = s.optString("dialogueTranslation", "..."),
                                sfxCue = s.optString("sfxCue", "Wind rustle"),
                                bgmCue = s.optString("bgmCue", "Cinematic strings"),
                                durationSec = s.optInt("durationSec", 8)
                            )
                        )
                    }
                }

                Result.success(
                    GeneratedAnimeProduction(
                        title = title,
                        synopsis = synopsis,
                        characters = charList,
                        scenes = sceneList
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateSceneKeyframeImage(
        prompt: String,
        stylePrompt: String,
        aspectRatio: String = "16:9",
        customApiKey: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(customApiKey)
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured for image generation.")
            )
        }

        // Use gemini-2.5-flash-image per skill instructions
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent?key=$apiKey"

        val completePrompt = "$prompt, $stylePrompt, high resolution, anime masterpiece, clean digital art, perfect composition"

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().put("text", completePrompt))
                    }
                    put("parts", partsArray)
                })
            }
            put("contents", contentsArray)
            put("generationConfig", JSONObject().apply {
                put("imageConfig", JSONObject().apply {
                    put("aspectRatio", aspectRatio)
                    put("imageSize", "1K")
                })
                put("responseModalities", JSONArray().apply {
                    put("TEXT")
                    put("IMAGE")
                })
            })
        }

        val request = Request.Builder()
            .url(endpoint)
            .post(requestJson.toString().toRequestBody(jsonMediaType))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val errorMsg = try {
                        val errObj = JSONObject(body).getJSONObject("error")
                        errObj.getString("message")
                    } catch (e: Exception) {
                        "HTTP ${response.code}: ${response.message}"
                    }
                    return@withContext Result.failure(Exception("Image API error: $errorMsg"))
                }

                val responseJson = JSONObject(body)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val content = candidates.getJSONObject(0).getJSONObject("content")
                    val parts = content.getJSONArray("parts")
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        if (part.has("inlineData")) {
                            val inlineData = part.getJSONObject("inlineData")
                            val base64Data = inlineData.getString("data")
                            return@withContext Result.success(base64Data)
                        }
                    }
                }
                Result.failure(Exception("No image returned from Gemini."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
