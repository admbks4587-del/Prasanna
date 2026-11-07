package com.example.data.model

enum class AnimeStyle(val displayName: String, val promptDescriptor: String) {
    UFOTABLE_SHONEN(
        "Modern Shonen (Ufotable/Mappa)",
        "masterpiece anime key visual, Ufotable dynamic lighting, high-contrast digital cel shading, vivid particle embers, hyper-detailed lineart, cinematic anime production"
    ),
    SHINKAI_CINEMATIC(
        "Makoto Shinkai Ethereal",
        "Makoto Shinkai aesthetic, luminous radiant sky, dramatic volumetric sunbeams, detailed urban reflections, pastel clouds, emotional anime romance cinematic"
    ),
    GHIBLI_CLASSIC(
        "Studio Ghibli Whimsical",
        "Studio Ghibli hand-painted aesthetic, lush organic watercolor landscapes, gentle pastoral charm, warm nostalgic hand-drawn animation style"
    ),
    CYBERPUNK_MECHA(
        "Cyberpunk & Mecha Dark",
        "dark cyberpunk anime, neon reflections in rain-soaked asphalt, intricate robotic mechanical details, holographic HUDs, gritty futuristic dystopia"
    ),
    RETRO_90S(
        "Retro 90s Cel-Shaded",
        "vintage 1990s anime cel art, Cowboy Bebop aesthetic, subtle analogue film grain, bold ink shadows, nostalgic retro color grading"
    ),
    DARK_FANTASY(
        "Dark Fantasy Gothic",
        "Jujutsu Gothic anime, sinister swirling cursed energy, ominous moonlit shadows, high-contrast crimson and deep purple aura, intense shonen battle"
    )
}

enum class ProjectDuration(val displayName: String, val durationSec: Long, val tag: String) {
    FIVE_MIN("5 Minutes", 5 * 60, "Quick Short"),
    FIFTEEN_MIN("15 Minutes", 15 * 60, "Special Pilot"),
    TWENTY_FOUR_MIN("24 Minutes", 24 * 60, "TV Episode (Standard)"),
    FORTY_FIVE_MIN("45 Minutes", 45 * 60, "OVA Feature"),
    NINETY_MIN("90 Minutes", 90 * 60, "Anime Movie"),
    TWO_HOURS("120 Minutes", 120 * 60, "Feature Film"),
    THREE_HOURS("180 Minutes", 180 * 60, "Extended Epic Cut")
}

enum class AnimeLanguage(val displayName: String, val langCode: String, val flag: String) {
    HINDI("Hindi (हिन्दी)", "hi-IN", "🇮🇳"),
    MARATHI("Marathi (मराठी)", "mr-IN", "🇮🇳"),
    ENGLISH("English (Global)", "en-US", "🌐"),
    JAPANESE("Japanese (日本語)", "ja-JP", "🇯🇵")
}

enum class AnimeResolution(val displayName: String, val width: Int, val height: Int) {
    HD_720P("720p HD", 1280, 720),
    FHD_1080P("1080p Full HD", 1920, 1080),
    UHD_4K("4K Ultra HD", 3840, 2160)
}

enum class AnimeAspectRatio(val displayName: String, val ratio: Float, val label: String) {
    WIDE_16_9("16:9 Standard", 16f / 9f, "16:9"),
    PORTRAIT_9_16("9:16 Shorts/Reels", 9f / 16f, "9:16"),
    CINEMATIC_2_39("2.39:1 CinemaScope", 2.39f, "2.39:1"),
    CLASSIC_4_3("4:3 Retro Classic", 4f / 3f, "4:3")
}

enum class AnimeFrameRate(val displayName: String, val fps: Int) {
    FPS_24("24 FPS (Anime Standard)", 24),
    FPS_30("30 FPS (Smooth Broadcast)", 30),
    FPS_60("60 FPS (High-Action)", 60)
}

enum class AudioScoring(val displayName: String, val description: String) {
    ORCHESTRAL_SHONEN("Epic Orchestral", "Thunderous brass, battle drums, and electric guitar solos"),
    SHINKAI_PIANO("Melancholic Piano", "Soft emotional piano, delicate strings, and rain sounds"),
    SYNTH_CYBERPUNK("Cyber Synthwave", "Pumping basslines, neon arpeggios, and futuristic industrial beats"),
    TRADITIONAL_JAPANESE("Traditional Shamisen", "Taiko drums, shakuhachi flute, and ancient spirit bells"),
    LOFI_AMBIENT("Lofi & Rain Ambience", "Relaxing acoustic chords, cafe chatter, and gentle raindrops")
}

enum class SubtitleOption(val displayName: String) {
    SOFT_SRT("Stylized Subtitles (SRT/VTT)"),
    HARD_GLOW("Glowing Neon Hardsubs"),
    BILINGUAL("Dual Language (Original + Dub)"),
    NONE("No Subtitles")
}

enum class CompressionTargetSize(val displayName: String, val targetMb: Int) {
    SIZE_40_MB("40 MB (Ultra-Compact)", 40),
    SIZE_100_MB("100 MB (Messaging/Discord)", 100),
    SIZE_250_MB("250 MB (Balanced Mobile HD)", 250),
    SIZE_500_MB("500 MB (High Quality)", 500),
    SIZE_1_GB("1 GB (Studio Master Archive)", 1024),
    CUSTOM("Custom Target Size...", 0)
}

enum class VideoCodec(val displayName: String, val efficiencyBonus: Float, val extension: String) {
    HEVC_H265("H.265 / HEVC (Recommended - High Efficiency)", 1.5f, "mp4"),
    AVC_H264("H.264 / AVC (Universal Device Fallback)", 1.0f, "mp4")
}

enum class PipelineStage(val stageName: String, val progress: Float) {
    IDLE("Ready for Story", 0.0f),
    STORY_ANALYSIS("1/6 • Analyzing Premise & Arc...", 0.15f),
    SCREENPLAY_EXPANSION("2/6 • Writing 3-Act Screenplay & Dialogue...", 0.35f),
    CHARACTER_DESIGN("3/6 • Designing Characters & Casting Voices...", 0.55f),
    STORYBOARD_GENERATION("4/6 • Directing Camera & Visual Beats...", 0.70f),
    VISUAL_SCENE_RENDER("5/6 • Rendering Anime Keyframes with AI...", 0.85f),
    AUDIO_SYNTHESIS("6/6 • Synthesizing Dialogue, SFX & Subtitles...", 0.95f),
    COMPOSITING("Finalizing Master Episode...", 0.98f),
    READY("Production Complete!", 1.0f),
    FAILED("Pipeline Stopped", 0.0f)
}
