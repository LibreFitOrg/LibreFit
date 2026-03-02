/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.components.modalBottomSheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.librefit.R
import org.librefit.enums.InfoMode
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.ui.components.MarkdownText
import org.librefit.ui.components.animations.AlarmLottie
import org.librefit.ui.components.animations.StatsLottie
import org.librefit.ui.components.animations.TrainingLottie
import org.librefit.ui.theme.LibreFitTheme

/** A modal bottom sheet which explains concepts to the user.
 * @param infoMode A [InfoMode] enum holding the info to display. If [infoMode] is equal to
 * [InfoMode.DISMISS], then InfoModalBottomSheet is not displayed
 * @param onDismiss A lambda function triggered when user leaves [InfoModalBottomSheet]
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InfoModalBottomSheet(
    infoMode: InfoMode,
    onDismiss: () -> Unit
) {
    if (infoMode != InfoMode.DISMISS) {
        val title = when (infoMode) {
            InfoMode.REST_TIMER -> stringResource(R.string.rest_time)
            InfoMode.TYPE_OF_SET -> stringResource(R.string.type_of_set)
            InfoMode.BEFORE_SAVING_STATS -> stringResource(R.string.statistics)
            InfoMode.MUSCLE_DISTRIBUTION -> stringResource(R.string.muscles_distribution)
            InfoMode.EXERCISES_DISTRIBUTION -> stringResource(R.string.exercises_distribution)
        }

        val text = when (infoMode) {
            InfoMode.REST_TIMER -> stringResource(R.string.rest_time_desc)
            InfoMode.TYPE_OF_SET -> stringResource(R.string.type_of_set_desc)
            InfoMode.BEFORE_SAVING_STATS -> stringResource(R.string.statistics_desc)
            InfoMode.MUSCLE_DISTRIBUTION -> stringResource(R.string.muscle_distribution_desc)
            InfoMode.EXERCISES_DISTRIBUTION -> stringResource(R.string.exercises_distribution_desc)
        }

        ModalBottomSheet(
            onDismissRequest = onDismiss
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineLargeEmphasized,
                        textAlign = TextAlign.Center
                    )
                }
                item {
                    Card(
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Column(
                            modifier = Modifier.padding(15.dp)
                        ) {
                            MarkdownText(text)
                        }
                    }
                }
                item {
                    when (infoMode) {
                        InfoMode.REST_TIMER -> AlarmLottie()
                        InfoMode.TYPE_OF_SET -> TrainingLottie()
                        InfoMode.BEFORE_SAVING_STATS -> StatsLottie()
                        InfoMode.MUSCLE_DISTRIBUTION -> StatsLottie()
                        InfoMode.EXERCISES_DISTRIBUTION -> StatsLottie()
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun InfoModalBottomSheetPreview() {
    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        InfoModalBottomSheet(InfoMode.EXERCISES_DISTRIBUTION) { }
    }
}