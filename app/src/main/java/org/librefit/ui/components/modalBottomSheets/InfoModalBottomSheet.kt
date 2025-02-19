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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

/** A modal bottom sheet used in [org.librefit.ui.screens.workout.WorkoutScreen] and
 * [org.librefit.ui.screens.edit.EditWorkoutScreen] to explain the types of set
 * and rest time in [ExerciseCard].
 * @param infoMode A [InfoMode] enum holding the info to display
 * @param onDismiss A lambda function in which [infoMode] should be set to [InfoMode.DISMISS]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoModalBottomSheet(
    infoMode: InfoMode,
    onDismiss: () -> Unit
) {
    val title = when (infoMode) {
        InfoMode.REST_TIMER -> stringResource(R.string.rest_time)
        InfoMode.TYPE_OF_SET -> stringResource(R.string.type_of_set)
        InfoMode.BEFORE_SAVING_STATS -> stringResource(R.string.statistics)
        InfoMode.DISMISS -> ""
    }


    val explanation = when (infoMode) {
        InfoMode.REST_TIMER -> stringResource(R.string.rest_time_desc)
        InfoMode.TYPE_OF_SET -> stringResource(R.string.type_of_set_desc)
        InfoMode.BEFORE_SAVING_STATS -> stringResource(R.string.statistics_desc)
        InfoMode.DISMISS -> ""
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge
            )
            HorizontalDivider()
            Text(
                text = explanation,
            )
            when (infoMode) {
                InfoMode.REST_TIMER -> AlarmLottie()
                InfoMode.TYPE_OF_SET -> TrainingLottie()
                InfoMode.BEFORE_SAVING_STATS -> StatsLottie()
                else -> {}
            }
        }
    }
}