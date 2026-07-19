/*
 *
 *  * SPDX-License-Identifier: GPL-3.0-or-later
 *  * Copyright (c) 2025-2026. The LibreFit Contributors
 *  *
 *  * LibreFit is subject to additional terms covering author attribution and trademark usage;
 *  * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 *
 */

package org.librefit.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import org.librefit.R
import org.librefit.enums.WarmupMode
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.ui.models.UiSet
import org.librefit.ui.models.UiWarmup
import org.librefit.ui.models.UiWarmupWithSets
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter
import org.librefit.util.Formatter.formatDetails

/**
 * This is a smaller version of [WarmupCard] when it is actually is a button. It is suitable to only show data of [UiWarmupWithSets]
 * but not to modify it.
 *
 * @param warmupWithSets A [UiWarmupWithSets] that holds the data
 * @param animatedVisibilityScope Used for image's animation transition
 * @param isRoutine When `false`, the card shows checkboxes of set completion
 */
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SharedTransitionScope.WarmupCardSmall(
    warmupWithSets: UiWarmupWithSets,
    isRoutine: Boolean = false,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    Button(
        onClick = {},
        modifier = Modifier
            .padding(5.dp),
        shapes = ButtonDefaults.shapes(
            shape = MaterialTheme.shapes.extraLarge
        ),
        contentPadding = ButtonDefaults.MediumContentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {

                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.warmup),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
            HorizontalDivider()

            Text(
                text = formatDetails(
                    stringResource(R.string.type_of_warmup),
                    stringResource(
                        Formatter.setWarmupModeToStringId(warmupWithSets.warmup.warmupMode)
                    )
                )
            )

            if (warmupWithSets.warmup.restTime != 0) {
                Text(
                    formatDetails(
                        stringResource(R.string.rest_time),
                        warmupWithSets.warmup.restTime.toString()
                                + " " + stringResource(R.string.seconds).replaceFirstChar { it.lowercase() })
                )
            }

            if (warmupWithSets.warmup.notes.isNotBlank()) {
                HorizontalDivider()

                Text(formatDetails(stringResource(R.string.notes), warmupWithSets.warmup.notes))
            }


            if (warmupWithSets.sets.isNotEmpty()) {
                val warmupMode = warmupWithSets.warmup.warmupMode
                ElevatedCard(
                    shape = MaterialTheme.shapes.largeIncreased,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(start = 15.dp)
                                .weight(1f),
                            text = stringResource(R.string.set)
                        )
                        when (warmupMode) {
                            WarmupMode.DEFAULT -> Text(
                                modifier = Modifier.weight(1.25f),
                                text = stringResource(R.string.load) + " (" + stringResource(
                                    R.string.kg
                                ) + ")"
                            )
                        }
                        Text(
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Left,
                            text = stringResource(R.string.reps)
                        )

                        if (!isRoutine) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = stringResource(R.string.done),
                                modifier = Modifier.weight(.5f),
                            )
                        }
                    }
                    Column {
                        warmupWithSets.sets.forEachIndexed { index, set ->
                            val backgroundColor by animateColorAsState(
                                targetValue = if (set.completed) {
                                    MaterialTheme.colorScheme.tertiaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHighest
                                },
                                label = "animated_color_for_set_background"
                            )
                            val contentColor by animateColorAsState(
                                targetValue = if (set.completed) {
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                label = "animated_color_for_set_content"
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = CornerSize(if (index == 0) 45 else 0),
                                            topEnd = CornerSize(if (index == 0) 45 else 0),
                                            bottomEnd = CornerSize(
                                                if (index == warmupWithSets.sets.lastIndex) 45 else 0
                                            ),
                                            bottomStart = CornerSize(
                                                if (index == warmupWithSets.sets.lastIndex) 45 else 0
                                            ),
                                        )
                                    )
                                    .background(backgroundColor)
                                    .padding(5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceAround,
                            ) {
                                Text(
                                    modifier = Modifier
                                        .padding(start = 15.dp)
                                        .weight(1f),
                                    text = "${index + 1}",
                                    color = contentColor
                                )

                                Text(
                                    modifier = Modifier.weight(1.25f),
                                    text =
                                        when (warmupMode) {
                                            WarmupMode.DEFAULT -> Formatter.normalizeNumericString(
                                                set.load.toString(),
                                                maxDecimalDigits = 2
                                            )
                                        },
                                    color = contentColor
                                )

                                Text(
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Left,
                                    text = "${set.reps}",
                                    color = contentColor
                                )

                                if (!isRoutine) {
                                    Checkbox(
                                        modifier = Modifier.weight(.5f),
                                        checked = set.completed,
                                        onCheckedChange = null
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun WarmupCardSmallPreview() {
    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                WarmupCardSmall(
                    warmupWithSets = UiWarmupWithSets(
                        warmup = UiWarmup(
                            notes = "Notes",
                            restTime = 100,
                            warmupMode = WarmupMode.DEFAULT
                        ),
                        sets = persistentListOf(
                            UiSet(load = 123.45, completed = true),
                            UiSet(reps = 10),
                            UiSet()
                        )
                    ),
                    animatedVisibilityScope = this
                )
            }
        }
    }
}