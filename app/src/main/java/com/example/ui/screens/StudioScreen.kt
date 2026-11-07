package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AnimeAspectRatio
import com.example.data.model.AnimeFrameRate
import com.example.data.model.AnimeLanguage
import com.example.data.model.AnimeResolution
import com.example.data.model.AnimeStyle
import com.example.data.model.AudioScoring
import com.example.data.model.ProjectDuration
import com.example.data.model.SubtitleOption
import com.example.ui.AnimeVerseViewModel
import com.example.ui.components.PipelineProgressCard
import com.example.ui.theme.CelestialGold
import com.example.ui.theme.CyberVioletPrimary
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun StudioScreen(
    viewModel: AnimeVerseViewModel,
    modifier: Modifier = Modifier
) {
    val storyInput by viewModel.storyInput.collectAsState()
    val selectedStyle by viewModel.selectedStyle.collectAsState()
    val selectedDuration by viewModel.selectedDuration.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val selectedAspectRatio by viewModel.selectedAspectRatio.collectAsState()
    val selectedResolution by viewModel.selectedResolution.collectAsState()
    val selectedFrameRate by viewModel.selectedFrameRate.collectAsState()
    val selectedAudioScore by viewModel.selectedAudioScore.collectAsState()
    val selectedSubtitle by viewModel.selectedSubtitle.collectAsState()
    val generationState by viewModel.generationState.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val selectedProjectId by viewModel.selectedProjectId.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 96.dp)
    ) {
        // Hero Cinematic Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(20.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.anime_hero_banner),
                contentDescription = "AnimeVerse AI Cinematic Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, SurfaceDark.copy(alpha = 0.9f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = CelestialGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "AUTONOMOUS ANIME ENGINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "Story Idea ➔ Full Anime Movie",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "Screenplay • AI Storyboards • Hindi/Marathi/Global Voices • Subtitles • MP4",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        // Pipeline Progress if currently running
        PipelineProgressCard(
            generationState = generationState,
            onRetry = { viewModel.startAnimeGeneration() }
        )

        // Story Input Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(CyberVioletPrimary.copy(alpha = 0.3f), ElectricCyan.copy(alpha = 0.3f)))
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Story Idea or Document",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }
                    Text(
                        text = "${storyInput.length} chars",
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = storyInput,
                    onValueChange = { viewModel.storyInput.value = it },
                    placeholder = {
                        Text(
                            text = "Enter any story premise or paste document here... (e.g., 'A cyberpunk samurai in futuristic Varanasi defends the ancient ghats from rogue nanite specters...')",
                            fontSize = 13.sp,
                            color = TextTertiary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("story_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberVioletPrimary,
                        unfocusedBorderColor = SurfaceElevated,
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Prompt Pills
                Text(
                    text = "Example Story Prompts:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    val examples = listOf(
                        "Neo-Varanasi 2099: Cyber-Samurai" to "In futuristic 2099 Varanasi, a cybernetic samurai with an ancient plasma katana defends the holy river shrines from rogue rogue android spirits.",
                        "Gojo Void Dimension Clash" to "A mysterious dimensional rift opens over Tokyo. A white-haired sorcerer with boundless infinity energy confronts an ancient cursed spirit awakening.",
                        "Doraemon 2112: Mecha Paradox" to "A futuristic robotic earless cat travels through a chronal rift with gadget tools to help a kind young student save future Tokyo from a rogue temporal vortex.",
                        "Spirit Shrine of Kyoto" to "On a rainy evening in Kyoto, an orphaned high school artist discovers a hidden spirit train station leading into the ethereal celestial realm."
                    )
                    examples.forEach { (label, prompt) ->
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceHighlight)
                                .border(1.dp, BorderGlowColor(selected = storyInput == prompt), RoundedCornerShape(20.dp))
                                .clickable { viewModel.populateStoryExample(prompt) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                color = if (storyInput == prompt) ElectricCyan else TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Production Parameters Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Anime Production Parameters",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 1. Duration (5 min to 3 hours)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Target Duration", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    ProjectDuration.values().forEach { duration ->
                        val isSel = selectedDuration == duration
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) CyberVioletPrimary.copy(alpha = 0.3f) else SurfaceElevated)
                                .border(1.dp, if (isSel) CyberVioletPrimary else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { viewModel.selectedDuration.value = duration }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Text(
                                    text = duration.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) ElectricCyan else TextPrimary
                                )
                                Text(
                                    text = duration.tag,
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Anime Visual Style
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = CyberVioletPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Anime Art & Studio Style", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    AnimeStyle.values().forEach { style ->
                        val isSel = selectedStyle == style
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) CyberVioletPrimary.copy(alpha = 0.35f) else SurfaceElevated)
                                .border(1.dp, if (isSel) ElectricCyan else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { viewModel.selectedStyle.value = style }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = style.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) TextPrimary else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Language Selection (Hindi, Marathi, English, Japanese)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Translate, contentDescription = null, tint = CelestialGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Dialogue & Dubbing Language", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    AnimeLanguage.values().forEach { lang ->
                        val isSel = selectedLanguage == lang
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) CyberVioletPrimary.copy(alpha = 0.3f) else SurfaceElevated)
                                .border(1.dp, if (isSel) CelestialGold else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { viewModel.selectedLanguage.value = lang }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${lang.flag} ${lang.displayName}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSel) CelestialGold else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Aspect Ratio & Resolution
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Aspect Ratio", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            AnimeAspectRatio.values().forEach { ratio ->
                                val isSel = selectedAspectRatio == ratio
                                Box(
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) ElectricCyan.copy(alpha = 0.25f) else SurfaceElevated)
                                        .border(1.dp, if (isSel) ElectricCyan else Color.Transparent, RoundedCornerShape(6.dp))
                                        .clickable { viewModel.selectedAspectRatio.value = ratio }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = ratio.label,
                                        fontSize = 11.sp,
                                        color = if (isSel) ElectricCyan else TextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Resolution & FPS", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            AnimeResolution.values().forEach { res ->
                                val isSel = selectedResolution == res
                                Box(
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) CyberVioletPrimary.copy(alpha = 0.25f) else SurfaceElevated)
                                        .border(1.dp, if (isSel) CyberVioletPrimary else Color.Transparent, RoundedCornerShape(6.dp))
                                        .clickable { viewModel.selectedResolution.value = res }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = res.displayName,
                                        fontSize = 11.sp,
                                        color = if (isSel) TextPrimary else TextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Big Primary Generate Button
        Button(
            onClick = { viewModel.startAnimeGeneration() },
            enabled = storyInput.isNotBlank() && !generationState.isGenerating,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 16.dp)
                .testTag("generate_anime_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyberVioletPrimary,
                contentColor = Color.White,
                disabledContainerColor = SurfaceElevated,
                disabledContentColor = TextTertiary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (generationState.isGenerating) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Generating Anime Production...",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = CelestialGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GENERATE FULL ANIME PRODUCTION",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Existing Projects List / Carousel
        if (allProjects.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Anime Productions in Studio (${allProjects.size})",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                allProjects.forEach { project ->
                    val isSelected = project.id == selectedProjectId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.selectProject(project.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) SurfaceElevated else SurfaceDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(ElectricCyan, CyberVioletPrimary))
                        ) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = project.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) ElectricCyan else TextPrimary,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${project.animeStyle} • ${project.durationMinutes} min • ${project.language}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberVioletPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isSelected) "ACTIVE" else "SELECT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) ElectricCyan else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun BorderGlowColor(selected: Boolean): Color {
    return if (selected) ElectricCyan else Color.Transparent
}
