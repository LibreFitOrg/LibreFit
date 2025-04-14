/*
 * Copyright (c) 2024-2025. LibreFit
 *
 * This file is part of LibreFit
 *
 * LibreFit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LibreFit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LibreFit.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.librefit.ui.components.modalBottomSheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.librefit.R
import org.librefit.enums.InfoMode
import org.librefit.ui.components.animations.AlarmLottie
import org.librefit.ui.components.animations.StatsLottie
import org.librefit.ui.components.animations.TrainingLottie

/** A modal bottom sheet which explains concepts to the user.
 * @param infoMode A [InfoMode] enum holding the info to display. If [infoMode] is equal to
 * [InfoMode.DISMISS], then InfoModalBottomSheet is not displayed
 * @param onDismiss A lambda function triggered when user leaves [InfoModalBottomSheet]
 */
@OptIn(ExperimentalMaterial3Api::class)
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
            else -> ""
        }

        val text = when (infoMode) {
            InfoMode.REST_TIMER -> stringResource(R.string.rest_time_desc)
            InfoMode.TYPE_OF_SET -> stringResource(R.string.type_of_set_desc)
            InfoMode.BEFORE_SAVING_STATS -> stringResource(R.string.statistics_desc)
            else -> ""
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
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
                item {
                    HorizontalDivider()
                }
                item {
                    Text(text)
                }
                item {
                    when (infoMode) {
                        InfoMode.REST_TIMER -> AlarmLottie()
                        InfoMode.TYPE_OF_SET -> TrainingLottie()
                        InfoMode.BEFORE_SAVING_STATS -> StatsLottie()
                        InfoMode.DISMISS -> {}
                    }
                }
            }
        }
    }
}