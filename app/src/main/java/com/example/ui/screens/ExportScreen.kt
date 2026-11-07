package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AnimeRenderJobEntity
import com.example.data.model.CompressionTargetSize
import com.example.data.model.VideoCodec
import com.example.ui.AnimeVerseViewModel
import com.example.ui.theme.CelestialGold
import com.example.ui.theme.CyberVioletPrimary
import com.example.ui.theme.DangerRuby
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SakuraPink
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WarningAmber

@Composable
fun ExportScreen(
    viewModel: AnimeVerseViewModel,
    modifier: Modifier = Modifier
) {
    val selectedTargetOption by viewModel.selectedTargetSizeOption.collectAsState()
    val customSizeMb by viewModel.customTargetSizeMb.collectAsState()
    val selectedCodec by viewModel.selectedCodec.collectAsState()
    val isRendering by viewModel.isRenderingJob.collectAsState()
    val renderProgress by viewModel.renderProgress.collectAsState()
    val renderJobs by viewModel.allRenderJobs.collectAsState()
    val selectedDuration by viewModel.selectedDuration.collectAsState()

    val plan = viewModel.calculateCurrentCompressionPlan()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 96.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Export & Smart Video Compression",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = "Bitrate calculation based on actual video duration (${selectedDuration.displayName}) and audio specs.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Target Size Selection
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "1. Select Target File Size",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        CompressionTargetSize.values().forEach { option ->
                            val isSel = selectedTargetOption == option
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) CyberVioletPrimary.copy(alpha = 0.35f) else SurfaceElevated)
                                    .border(1.dp, if (isSel) ElectricCyan else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.selectedTargetSizeOption.value = option }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = option.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) TextPrimary else TextSecondary
                                )
                            }
                        }
                    }

                    if (selectedTargetOption == CompressionTargetSize.CUSTOM) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = customSizeMb,
                            onValueChange = { viewModel.customTargetSizeMb.value = it },
                            label = { Text("Target Size (in MB)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Video Codec Selection (H.265 / HEVC vs H.264)
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "2. Encoding Codec",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    VideoCodec.values().forEach { codec ->
                        val isSel = selectedCodec == codec
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) CyberVioletPrimary.copy(alpha = 0.25f) else SurfaceElevated)
                                .border(1.dp, if (isSel) CyberVioletPrimary else Color.Transparent, RoundedCornerShape(10.dp))
                                .clickable { viewModel.selectedCodec.value = codec }
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = codec.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) ElectricCyan else TextPrimary
                                    )
                                    Text(
                                        text = if (codec == VideoCodec.HEVC_H265) "Preserves crisp anime linework at half the bitrate." else "Plays on older media players without hardware HEVC.",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Dynamic Bitrate Calculation Matrix
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(CyberVioletPrimary.copy(alpha = 0.4f), ElectricCyan.copy(alpha = 0.4f)))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Dynamic Bitrate Calculation",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (plan.qualityGrade.isSevere) DangerRuby.copy(alpha = 0.25f) else CyberVioletPrimary.copy(alpha = 0.25f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = plan.qualityGrade.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (plan.qualityGrade.isSevere) DangerRuby else ElectricCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BitrateStatItem(title = "Video Bitrate", value = "${plan.videoBitrateKbps} kbps")
                        BitrateStatItem(title = "Audio Bitrate", value = "${plan.audioBitrateKbps} kbps")
                        BitrateStatItem(title = "Auto Resolution", value = plan.recommendedResolution)
                        BitrateStatItem(title = "Frame Rate", value = "${plan.recommendedFps} FPS")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Estimated Output: ~${plan.estimatedOutputSizeMb} MB",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CelestialGold
                        )
                        Text(
                            text = "Target Cap: ${plan.targetSizeMb.toInt()} MB",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    // Honest Quality Warning Callout
                    if (plan.qualityWarning != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (plan.qualityGrade.isSevere) DangerRuby.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f))
                                .border(1.dp, if (plan.qualityGrade.isSevere) DangerRuby else WarningAmber, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (plan.qualityGrade.isSevere) DangerRuby else WarningAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = plan.qualityWarning,
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Start Render Button
            Button(
                onClick = { viewModel.startCompressionRenderJob() },
                enabled = !isRendering,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_render_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberVioletPrimary),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isRendering) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Rendering & Compressing (${(renderProgress * 100).toInt()}%)...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                } else {
                    Icon(imageVector = Icons.Default.MovieCreation, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "START RENDER & COMPRESS MP4",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            if (isRendering) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { renderProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = ElectricCyan,
                    trackColor = SurfaceElevated
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Render Queue & Completed Exports (${renderJobs.size})",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (renderJobs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No jobs rendered yet. Choose a target size above and click Start Render.",
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                }
            }
        } else {
            items(renderJobs, key = { it.id }) { job ->
                RenderJobCard(
                    job = job,
                    onDelete = { viewModel.deleteRenderJob(job) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun BitrateStatItem(title: String, value: String) {
    Column {
        Text(text = title, fontSize = 10.sp, color = TextTertiary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
private fun RenderJobCard(
    job: AnimeRenderJobEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = job.projectTitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (job.status == "COMPLETED") CyberVioletPrimary.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = job.status,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (job.status == "COMPLETED") ElectricCyan else WarningAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "${job.targetSizeMb} MB Cap • ${job.codec.take(6)} • ${job.finalResolution} @ ${job.finalFps}fps",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Text(
                    text = "Bitrate: ${job.calculatedVideoBitrateKbps}k video, ${job.calculatedAudioBitrateKbps}k audio • Actual: ${"%.1f".format(job.actualSizeMb)} MB",
                    fontSize = 10.sp,
                    color = CelestialGold
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Job",
                    tint = TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
