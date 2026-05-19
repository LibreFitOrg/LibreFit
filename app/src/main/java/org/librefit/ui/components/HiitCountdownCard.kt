/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.librefit.ui.screens.workout.WorkoutScreenViewModel.HiitPhase

/**
 * A card that displays a large circular countdown timer for HIIT-style exercises.
 *
 * Shows the current set number, exercise name, a big countdown arc, and play/skip controls.
 * The arc is orange during a set countdown, blue during rest, and green when done.
 */
@Composable
fun HiitCountdownCard(
    exerciseName: String,
    currentSetIndex: Int,
    totalSets: Int,
    countdownSeconds: Int,
    countdownTotal: Int,
    restSeconds: Int,
    restTotal: Int,
    hiitPhase: HiitPhase,
    onPlayPressed: () -> Unit,
    onCancelPressed: () -> Unit,
    onSkipRest: () -> Unit,
    onInfoPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSetPhase = hiitPhase is HiitPhase.SetCountdown
    val isRestPhase = hiitPhase is HiitPhase.RestBetweenSets
    val isIdle = hiitPhase is HiitPhase.Idle
    val isDone = hiitPhase is HiitPhase.ExerciseDone

    // Arc progress
    val displaySeconds = when {
        isSetPhase -> countdownSeconds
        isRestPhase -> restSeconds
        else -> 0
    }
    val displayTotal = when {
        isSetPhase -> countdownTotal
        isRestPhase -> restTotal
        else -> 1
    }
    val progress by animateFloatAsState(
        targetValue = if (displayTotal > 0) displaySeconds.toFloat() / displayTotal else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "countdown_progress"
    )

    // Colors
    val arcColor by animateColorAsState(
        targetValue = when {
            isRestPhase -> Color(0xFF42A5F5)  // Blue for rest
            isDone -> Color(0xFF66BB6A)       // Green for done
            countdownSeconds <= 3 && isSetPhase -> Color(0xFFEF5350) // Red for last 3 seconds
            else -> Color(0xFFFF7043)         // Orange for set
        },
        animationSpec = tween(300),
        label = "arc_color"
    )

    val phaseLabel = when {
        isSetPhase -> "SET"
        isRestPhase -> "REST"
        isDone -> "DONE"
        else -> "READY"
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header row: exercise name + set counter + info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = exerciseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Set ${currentSetIndex + 1} / $totalSets",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onInfoPressed) {
                    Icon(Icons.Default.Info, contentDescription = "Exercise details")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phase label
            Text(
                text = phaseLabel,
                style = MaterialTheme.typography.labelLarge,
                color = arcColor,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Countdown arc
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                Canvas(modifier = Modifier.size(200.dp)) {
                    val strokeWidth = 12.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    // Background track
                    drawArc(
                        color = Color.Gray.copy(alpha = 0.2f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Progress arc
                    drawArc(
                        color = arcColor,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Time text
                val minutes = displaySeconds / 60
                val seconds = displaySeconds % 60
                Text(
                    text = if (isDone) "Done!" else "%d:%02d".format(minutes, seconds),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDone) Color(0xFF66BB6A) else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Controls
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                when {
                    isIdle -> {
                        FilledTonalButton(onClick = onPlayPressed) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Start Set")
                        }
                    }

                    isSetPhase -> {
                        FilledTonalButton(onClick = onCancelPressed) {
                            Icon(Icons.Default.Pause, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Cancel")
                        }
                    }

                    isRestPhase -> {
                        FilledTonalButton(onClick = onSkipRest) {
                            Icon(Icons.Default.SkipNext, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Skip Rest")
                        }
                    }

                    isDone -> {
                        Text(
                            text = "All sets complete! Move to next exercise.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
