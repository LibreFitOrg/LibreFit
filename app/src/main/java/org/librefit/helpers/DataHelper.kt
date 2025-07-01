/*
 * Copyright (c) 2025. LibreFit
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

package org.librefit.helpers

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.librefit.data.ChartData
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.db.repository.MeasurementRepository
import org.librefit.enums.SetMode
import org.librefit.enums.chart.WorkoutChart
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import javax.inject.Inject

class DataHelper @Inject constructor(
    private val measurementRepository: MeasurementRepository
) {
    val shortFormatter: DateTimeFormatter? =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(
            Locale.getDefault()
        )

    /**
     * It returns a list of [ChartData] corresponding to the given [workoutChart] type
     * for each workout in [workoutsWithExercises].
     *
     * Each entry’s X-value is the formatted completion date of the workout, and Y-value is:
     *  - [WorkoutChart.DURATION]: total minutes elapsed in the workout
     *  - [WorkoutChart.VOLUME] : total volume lifted, computed as sum(weight * reps) for completed sets.
     *      When an exercise’s set mode is `SetMode.REPS` or `SetMode.LOAD_AND_BODY_WEIGHT`,
     *      the user’s body weight at the time of the workout is added.
     *  - [WorkoutChart.REPS]: total number of reps completed in the workout
     *
     * This function concurrently:
     *  1. Retrieves the latest user’s [org.librefit.db.entity.Measurement.bodyWeight] at each [org.librefit.db.entity.Workout.completed] date.
     *  2. Builds the corresponding [ChartData] items.
     *
     * @param workoutChart The metric to chart (duration, volume, or reps).
     * @param workoutsWithExercises A list of workouts paired with their exercises and sets.
     * @return A list of [ChartData]
     */
    suspend fun fetchListChartData(
        workoutChart: WorkoutChart,
        workoutsWithExercises: List<WorkoutWithExercisesAndSets>
    ): List<ChartData> = coroutineScope {
        val bodyWeights = workoutsWithExercises
            .map {
                async {
                    measurementRepository.getLastMeasurementByCutoff(it.workout.completed)?.bodyWeight
                        ?: 0f
                }
            }
            .awaitAll()

        workoutsWithExercises
            .mapIndexed { index, it ->
                async {
                    ChartData(
                        yValue = when (workoutChart) {
                            WorkoutChart.DURATION -> it.workout.timeElapsed / 60f
                            WorkoutChart.VOLUME -> it.exercisesWithSets.sumOf { exe ->
                                exe.sets.filter { it.completed }.sumOf {
                                    (it.load + if (exe.exercise.setMode == SetMode.BODYWEIGHT ||
                                        exe.exercise.setMode == SetMode.BODYWEIGHT_WITH_LOAD
                                    )
                                        bodyWeights[index] else 0f) * it.reps.toDouble()
                                }
                            }

                            WorkoutChart.REPS -> it.exercisesWithSets.sumOf { exe ->
                                exe.sets.filter { it.completed }.sumOf { it.reps }
                            }
                        }.toFloat(),
                        xValue = it.workout.completed.format(shortFormatter)
                    )
                }
            }
            .awaitAll()

    }

    suspend fun fetchVolumeFromWorkout(
        workout: WorkoutWithExercisesAndSets
    ): Float {
        val isRoutine = workout.workout.routine

        val bodyWeight = measurementRepository.getLastMeasurementByCutoff(
            if (isRoutine) workout.workout.created else workout.workout.completed
        )?.bodyWeight ?: 0f

        return workout.exercisesWithSets.sumOf { exe ->
            exe.sets.sumOf { set ->
                val volumeForEachRep = when (exe.exercise.setMode) {
                    SetMode.LOAD_ONLY -> if (isRoutine || set.completed) set.load else 0f
                    SetMode.BODYWEIGHT -> if (isRoutine || set.completed) bodyWeight else 0f
                    SetMode.BODYWEIGHT_WITH_LOAD -> (if (isRoutine || set.completed) set.load else 0f) + bodyWeight
                    SetMode.DURATION -> 0f
                }

                (volumeForEachRep * set.reps).toDouble()
            }
        }.toFloat()
    }
}