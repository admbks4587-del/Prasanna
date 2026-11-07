package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AnimeCharacterEntity
import com.example.ui.AnimeVerseViewModel
import com.example.ui.theme.CelestialGold
import com.example.ui.theme.CyberVioletPrimary
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SakuraPink
import com.example.ui.theme.ShonenCrimson
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WarningAmber

@Composable
fun CharacterScreen(
    viewModel: AnimeVerseViewModel,
    modifier: Modifier = Modifier
) {
    val characters by viewModel.currentCharacters.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 96.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Character Library & Voice Casting",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = "Consistent character designs, personality profiles & multilingual synthetic voices.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Ethical AI Voice Disclaimer Callout
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(WarningAmber.copy(alpha = 0.6f), CelestialGold.copy(alpha = 0.6f)))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Voice Compliance",
                        tint = WarningAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Ethical Voice Policy & Legal Compliance",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarningAmber
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "For familiar characters (e.g., Doraemon, Gojo, Goku), AnimeVerse AI generates original synthetic voices using non-identifying vocal traits. The platform does not claim to use or copy original voice actors without explicit authorization.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        if (characters.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No characters cast yet. Generate a story in Studio to populate the cast.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(characters, key = { it.id }) { character ->
                CharacterCard(
                    character = character,
                    onAudition = { sample ->
                        viewModel.auditionCharacterVoice(character, sample)
                    },
                    onUpdate = { updated ->
                        viewModel.updateCharacter(updated)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun CharacterCard(
    character: AnimeCharacterEntity,
    onAudition: (String) -> Unit,
    onUpdate: (AnimeCharacterEntity) -> Unit
) {
    var pitch by remember(character.voicePitch) { mutableFloatStateOf(character.voicePitch) }
    var speed by remember(character.voiceSpeed) { mutableFloatStateOf(character.voiceSpeed) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("character_card_${character.name.replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(CyberVioletPrimary.copy(alpha = 0.3f), SakuraPink.copy(alpha = 0.3f))
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row with Avatar & Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(CyberVioletPrimary, ElectricCyan)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = character.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when (character.role.lowercase()) {
                                        "protagonist" -> CyberVioletPrimary.copy(alpha = 0.3f)
                                        "mentor" -> CelestialGold.copy(alpha = 0.3f)
                                        "antagonist" -> ShonenCrimson.copy(alpha = 0.3f)
                                        else -> ElectricCyan.copy(alpha = 0.3f)
                                    }
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = character.role.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = when (character.role.lowercase()) {
                                    "protagonist" -> CyberVioletPrimary
                                    "mentor" -> CelestialGold
                                    "antagonist" -> ShonenCrimson
                                    else -> ElectricCyan
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = character.personality,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Appearance prompt description
            Text(
                text = "VISUAL DESIGN: ${character.appearancePrompt}",
                fontSize = 11.sp,
                color = TextTertiary,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Voice casting and audition section
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Synthetic Voice: ${character.voiceProfile}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Button(
                            onClick = {
                                val sample = "नमस्ते, मैं तैयार हूँ अपने सफर के लिए! Let's embark on our anime adventure!"
                                onAudition(sample)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberVioletPrimary),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("audition_voice_${character.name.replace(" ", "_")}")
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Audition", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pitch Slider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Pitch: ${"%.2f".format(pitch)}x",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            modifier = Modifier.width(70.dp)
                        )
                        Slider(
                            value = pitch,
                            onValueChange = {
                                pitch = it
                                onUpdate(character.copy(voicePitch = it))
                            },
                            valueRange = 0.6f..1.6f,
                            colors = SliderDefaults.colors(
                                thumbColor = ElectricCyan,
                                activeTrackColor = CyberVioletPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Speed Slider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Speed: ${"%.2f".format(speed)}x",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            modifier = Modifier.width(70.dp)
                        )
                        Slider(
                            value = speed,
                            onValueChange = {
                                speed = it
                                onUpdate(character.copy(voiceSpeed = it))
                            },
                            valueRange = 0.7f..1.4f,
                            colors = SliderDefaults.colors(
                                thumbColor = CelestialGold,
                                activeTrackColor = CelestialGold
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (!character.legalVoiceDisclaimer.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "⚖️ ${character.legalVoiceDisclaimer}",
                    fontSize = 10.sp,
                    color = TextTertiary
                )
            }
        }
    }
}
