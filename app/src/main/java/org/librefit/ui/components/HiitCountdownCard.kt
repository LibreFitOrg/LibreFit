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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.librefit.R
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.ui.screens.workout.HiitPhase
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter

/**
 * Card showing a circular countdown timer for an HIIT-style exercise.
 *
 * The card has four visual states driven by [phase]:
 *  - [HiitPhase.Idle]:           the user can press play to start.
 *  - [HiitPhase.SetCountdown]:   counts down the set duration (primary color).
 *  - [HiitPhase.RestBetweenSets]: counts down the rest period (tertiary color).
 *  - [HiitPhase.ExerciseDone]:   all sets complete (secondary color).
 *
 * The composable is purely visual — all transitions are owned by the caller. This makes it
 * trivial to preview and to unit-test the state machine separately.
 *
 * @param exerciseName Name displayed in the header.
 * @param currentSetIndex 0-based index of the set the countdown applies to.
 * @param totalSets Total number of sets for the exercise.
 * @param countdownSeconds Remaining seconds in the active set countdown.
 * @param countdownTotal Total seconds for the active set countdown (used for progress).
 * @param restSeconds Remaining seconds in the rest period.
 * @param restTotal Total seconds for the rest period (used for progress).
 * @param phase Current [HiitPhase].
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
    phase: HiitPhase,
    onPlayPressed: () -> Unit,
    onCancelPressed: () -> Unit,
    onSkipRest: () -> Unit,
    onInfoPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displaySeconds = when (phase) {
        is HiitPhase.SetCountdown -> countdownSeconds
        is HiitPhase.RestBetweenSets -> restSeconds
        else -> 0
    }
    val displayTotal = when (phase) {
        is HiitPhase.SetCountdown -> countdownTotal.coerceAtLeast(1)
        is HiitPhase.RestBetweenSets -> restTotal.coerceAtLeast(1)
        else -> 1
    }
    val progress by animateFloatAsState(
        targetValue = displaySeconds.toFloat() / displayTotal,
        animationSpec = tween(durationMillis = 300),
        label = "hiit_countdown_progress"
    )

    val arcColor by animateColorAsState(
        targetValue = when (phase) {
            is HiitPhase.RestBetweenSets -> MaterialTheme.colorScheme.tertiary
            is HiitPhase.ExerciseDone -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(300),
        label = "hiit_arc_color"
    )

    val phaseLabel = stringResource(
        when (phase) {
            is HiitPhase.SetCountdown -> R.string.hiit_phase_set
            is HiitPhase.RestBetweenSets -> R.string.hiit_phase_rest
            is HiitPhase.ExerciseDone -> R.string.hiit_phase_done
            HiitPhase.Idle -> R.string.hiit_phase_ready
        }
    )

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
                        text = stringResource(
                            R.string.hiit_set_progress, currentSetIndex + 1, totalSets
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onInfoPressed) {
                    Icon(
                        painter = painterResource(R.drawable.ic_info),
                        contentDescription = stringResource(R.string.exercise_details)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = phaseLabel,
                style = MaterialTheme.typography.labelLarge,
                color = arcColor,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            val trackColor = MaterialTheme.colorScheme.surfaceVariant
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                Canvas(modifier = Modifier.size(200.dp)) {
                    val strokeWidth = 12.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
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

                // Show MM:SS during countdowns; show "Done" label when complete.
                Text(
                    text = if (phase is HiitPhase.ExerciseDone) {
                        stringResource(R.string.hiit_phase_done)
                    } else {
                        Formatter.formatTime(displaySeconds).substring(3)
                    },
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (phase is HiitPhase.ExerciseDone) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                when (phase) {
                    HiitPhase.Idle -> {
                        FilledTonalButton(onClick = onPlayPressed) {
                            Icon(
                                painter = painterResource(R.drawable.ic_play_arrow),
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.hiit_start_set))
                        }
                    }

                    is HiitPhase.SetCountdown -> {
                        FilledTonalButton(onClick = onCancelPressed) {
                            Icon(
                                painter = painterResource(R.drawable.ic_pause),
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.cancel_dialog))
                        }
                    }

                    is HiitPhase.RestBetweenSets -> {
                        FilledTonalButton(onClick = onSkipRest) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_forward),
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.hiit_skip_rest))
                        }
                    }

                    is HiitPhase.ExerciseDone -> {
                        Text(
                            text = stringResource(R.string.hiit_all_sets_complete),
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

@Preview
@Composable
private fun HiitCountdownCardSetCountdownPreview() {
    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        HiitCountdownCard(
            exerciseName = "Mountain Climbers",
            currentSetIndex = 1,
            totalSets = 4,
            countdownSeconds = 18,
            countdownTotal = 30,
            restSeconds = 0,
            restTotal = 15,
            phase = HiitPhase.SetCountdown(exerciseId = 1L, setIndex = 1),
            onPlayPressed = {},
            onCancelPressed = {},
            onSkipRest = {},
            onInfoPressed = {}
        )
    }
}

@Preview
@Composable
private fun HiitCountdownCardRestPreview() {
    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        HiitCountdownCard(
            exerciseName = "Burpees",
            currentSetIndex = 2,
            totalSets = 4,
            countdownSeconds = 0,
            countdownTotal = 30,
            restSeconds = 8,
            restTotal = 15,
            phase = HiitPhase.RestBetweenSets(exerciseId = 1L, nextSetIndex = 2),
            onPlayPressed = {},
            onCancelPressed = {},
            onSkipRest = {},
            onInfoPressed = {}
        )
    }
}

@Preview
@Composable
private fun HiitCountdownCardIdlePreview() {
    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        HiitCountdownCard(
            exerciseName = "Jumping Jacks",
            currentSetIndex = 0,
            totalSets = 3,
            countdownSeconds = 0,
            countdownTotal = 30,
            restSeconds = 0,
            restTotal = 15,
            phase = HiitPhase.Idle,
            onPlayPressed = {},
            onCancelPressed = {},
            onSkipRest = {},
            onInfoPressed = {}
        )
    }
}

@Preview
@Composable
private fun HiitCountdownCardDonePreview() {
    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        HiitCountdownCard(
            exerciseName = "Plank",
            currentSetIndex = 3,
            totalSets = 4,
            countdownSeconds = 0,
            countdownTotal = 30,
            restSeconds = 0,
            restTotal = 15,
            phase = HiitPhase.ExerciseDone,
            onPlayPressed = {},
            onCancelPressed = {},
            onSkipRest = {},
            onInfoPressed = {}
        )
    }
}
