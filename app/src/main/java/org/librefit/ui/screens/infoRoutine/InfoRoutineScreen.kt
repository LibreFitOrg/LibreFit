/*
 * Copyright (c) 2024. LibreFit
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

package org.librefit.ui.screens.infoRoutine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.ui.components.CustomScaffold
import org.librefit.util.ExerciseDC

@Composable
fun InfoRoutineScreen(
    workoutId: Int = 0,
    list: List<ExerciseDC>,
    navController: NavHostController
) {
    val viewModel: InfoRoutineScreenViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass == InfoRoutineScreenViewModel::class.java) {
                    "Unknown ViewModel class"
                }
                @Suppress("UNCHECKED_CAST")
                return InfoRoutineScreenViewModel(workoutId, list) as T
            }
        }
    )

    CustomScaffold(
        title = "Info routine",
        navigateBack = { navController.popBackStack() },
        actions = listOf({}, {}),
        actionsIcons = listOf(Icons.Default.Edit, Icons.Default.Delete),
        actionsElevated = listOf(false, false),
        actionsEnabled = listOf(false, false)
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(viewModel.getExercises()) { exercise ->
                Text(exercise.exerciseDC.name)
            }
        }
    }
}

@Preview
@Composable
private fun InfoRoutineScreenPreview() {
    InfoRoutineScreen(
        list = listOf(),
        navController = rememberNavController()
    )
}