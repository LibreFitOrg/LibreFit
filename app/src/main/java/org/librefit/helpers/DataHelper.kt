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
 *
 * LibreFit is subject to additional terms covering author attribution and
 * trademark usage, as found in the accompanying ADDITIONAL_TERMS.md file.
 */

package org.librefit.helpers

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.db.repository.MeasurementRepository
import org.librefit.enums.SetMode
import org.librefit.enums.WorkoutState
import org.librefit.enums.chart.BodyweightChart
import org.librefit.enums.chart.ExerciseChart
import org.librefit.enums.chart.LoadChart
import org.librefit.enums.chart.StatisticsChart
import org.librefit.enums.chart.TimeChart
import org.librefit.enums.chart.WeightedBodyweightChart
import org.librefit.enums.chart.WorkoutChart
import org.librefit.ui.components.charts.Point
import org.librefit.util.Formatter
import org.librefit.util.OneRepMaxCalculator
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataHelper @Inject constructor(
    private val measurementRepository: MeasurementRepository,
    @param:ApplicationContext private val context: Context
) {
    val shortFormatter: DateTimeFormatter? =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(
            Locale.getDefault()
        )

    /**
     * It returns a list of [Point] corresponding to the given [workoutChart] type
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
     *  2. Builds the corresponding [Point] items.
     *
     * @param workoutChart The metric to chart (duration, volume, or reps).
     * @param workoutsWithExercises A list of workouts paired with their exercises and sets.
     * @return A list of [Point]
     */
    suspend fun fetchPointsForWorkoutsChart(
        workoutChart: WorkoutChart,
        workoutsWithExercises: List<WorkoutWithExercisesAndSets>
    ): List<Point> = coroutineScope {
        workoutsWithExercises
            .map { it ->
                async {
                    val bodyWeight = measurementRepository.getLastMeasurementByCutoff(
                        it.workout.completed
                    )?.bodyWeight ?: 0.0

                    Point(
                        yValues = listOf(
                            when (workoutChart) {
                                WorkoutChart.DURATION -> it.workout.timeElapsed / 60.0
                                WorkoutChart.VOLUME -> it.exercisesWithSets.sumOf { exe ->
                                    val includeBodyweight =
                                        exe.exercise.setMode == SetMode.BODYWEIGHT ||
                                                exe.exercise.setMode == SetMode.BODYWEIGHT_WITH_LOAD

                                    exe.sets.filter { it.completed }.sumOf {
                                        (it.load + if (includeBodyweight) bodyWeight else 0.0) * it.reps
                                    }
                                }
                                WorkoutChart.REPS -> it.exercisesWithSets.sumOf { exe ->
                                    exe.sets.filter { it.completed }.sumOf { it.reps }
                                }.toDouble()
                            }
                        ),
                        xValue = it.workout.completed.format(shortFormatter),
                        workoutId = it.workout.id
                    )
                }
            }
            .awaitAll()

    }

    suspend fun fetchVolumeFromWorkout(
        workout: WorkoutWithExercisesAndSets
    ): Double {
        val isRoutine = workout.workout.state == WorkoutState.ROUTINE

        val bodyWeight = measurementRepository.getLastMeasurementByCutoff(
            if (isRoutine) workout.workout.created else workout.workout.completed
        )?.bodyWeight ?: 0.0

        return workout.exercisesWithSets.sumOf { exe ->
            exe.sets.sumOf { set ->
                val volumeForEachRep = when (exe.exercise.setMode) {
                    SetMode.LOAD -> if (isRoutine || set.completed) set.load else 0.0
                    SetMode.BODYWEIGHT -> if (isRoutine || set.completed) bodyWeight else 0.0
                    SetMode.BODYWEIGHT_WITH_LOAD -> if (isRoutine || set.completed) set.load + bodyWeight else 0.0
                    SetMode.DURATION -> 0.0
                }

                (volumeForEachRep * set.reps)
            }
        }
    }


    /**
     * Calculates the distribution for each muscle group, categorized into dynamic time buckets.
     *
     * This function is highly configurable, allowing for any number of time buckets to be defined
     * via cutoffs and providing an option to include or exclude data that falls outside all specified
     * time periods.
     *
     * @param muscleDistributionStatisticsChart It defines which value has to be calculated based on
     * the enum passed: [StatisticsChart.LOAD], [StatisticsChart.DURATION] and so on.
     * @param workoutsWithExercises The list of user workouts to process.
     * @param muscleDistributionCutoffs A list of [LocalDateTime] points that define the boundaries for time buckets.
     *        The function creates `cutoffs.size` buckets for workouts newer than each cutoff, plus
     *        an optional final bucket for all older workouts. The list is automatically sorted internally.
     *        Example: Providing 2 cutoffs creates buckets for workouts newer than `cutoff[0]`,
     *        newer than `cutoff[1]`, and optionally all others (which are older).
     * @param includeOlderWorkouts If `true`, workouts older than all specified cutoffs are aggregated
     * into a final "older" bucket. If `false`, these workouts are entirely excluded from the results.
     * @return A list where each [Point] contains in [Point.yValues] a list of its average performance
     * for each time bucket while in [Point.xValue] there is the associated muscle.
     * The size of the `averages` list corresponds to the number of time buckets generated based on [muscleDistributionCutoffs].
     */
    suspend fun fetchMuscleDistributionWithTimeBuckets(
        muscleDistributionStatisticsChart: StatisticsChart,
        workoutsWithExercises: List<WorkoutWithExercisesAndSets>,
        muscleDistributionCutoffs: List<LocalDateTime>,
        includeOlderWorkouts: Boolean = false
    ): List<Point> {
        if (workoutsWithExercises.isEmpty()) {
            return emptyList()
        }

        val sortedCutoffs = muscleDistributionCutoffs.sortedDescending()
        // The number of buckets depends on whether the final "older" bucket is included.
        val numTimeBuckets =
            if (includeOlderWorkouts) sortedCutoffs.size + 1 else sortedCutoffs.size

        val processedData = coroutineScope {
            // Map each workout to a Deferred result.
            val deferredResults = workoutsWithExercises.map { w ->
                async {
                    // Find the index of the first cutoff the workout date is after.
                    val cutoffIndex = sortedCutoffs.indexOfFirst { w.workout.completed.isAfter(it) }

                    // Determine the final bucket index, or decide to discard the workout.
                    // A `null` index will signify that the workout should be discarded.
                    val timeBucketIndex: Int? = when {
                        // Case 1: The workout falls into one of the defined cutoff buckets.
                        cutoffIndex != -1 -> cutoffIndex
                        // Case 2: It's an older workout, and it is configured to include them.
                        includeOlderWorkouts -> sortedCutoffs.size // This is the last "older" bucket.
                        // Case 3: It's an older workout, and it is configured to EXCLUDE them.
                        else -> null
                    }

                    // If timeBucketIndex is null, return an empty list for this workout.
                    // Otherwise, process the exercises.
                    if (timeBucketIndex == null) {
                        emptyList()
                    } else {
                        // Process these exercises in order to calculate the specified value for each muscle
                        w.exercisesWithSets.mapNotNull { e ->
                            val muscles =
                                e.exerciseDC.primaryMuscles + e.exerciseDC.secondaryMuscles
                            val sets = e.sets.filter { it.completed }
                            val exercise = e.exercise

                            val bodyWeight = measurementRepository.getLastMeasurementByCutoff(
                                w.workout.completed
                            )?.bodyWeight ?: 0.0

                            val value = when (muscleDistributionStatisticsChart) {
                                StatisticsChart.LOAD -> sets.sumOf {
                                    when (exercise.setMode) {
                                        SetMode.LOAD -> it.load
                                        SetMode.BODYWEIGHT -> bodyWeight
                                        SetMode.BODYWEIGHT_WITH_LOAD -> it.load + bodyWeight
                                        SetMode.DURATION -> 0
                                    }.toDouble()
                                }
                                StatisticsChart.REPS -> sets.sumOf { it.reps }
                                StatisticsChart.VOLUME -> sets.sumOf {
                                    when (exercise.setMode) {
                                        SetMode.LOAD -> it.load
                                        SetMode.BODYWEIGHT -> bodyWeight
                                        SetMode.BODYWEIGHT_WITH_LOAD -> it.load + bodyWeight
                                        SetMode.DURATION -> 0
                                    }.toDouble() * it.reps
                                }
                                StatisticsChart.DURATION -> sets.sumOf { it.elapsedTime }
                            }.toDouble()

                            if (value == 0.0) {
                                null // Filter out zero-value
                            } else {
                                // Create a encoded list for the value's time bucket.
                                val values = MutableList(numTimeBuckets) { 0.0 }.apply {
                                    set(timeBucketIndex, value)
                                }.toList()
                                // Pair the muscles with the calculated value list.
                                muscles.toSet() to values
                            }
                        }
                    }
                }
            }
            // Wait for all parallel jobs to complete while
            // `flatten()` converts the List<List<Pair>> into a single List<Pair>.
            deferredResults.awaitAll().flatten()
        }


        return processedData
            .asSequence()
            // Unroll the data: from (Set<Muscle>, List) to multiple (Muscle, List) pairs.
            .flatMap { (muscles, valuesList) ->
                muscles.asSequence().map { muscle -> muscle to valuesList }
            }
            // Group all value lists by muscle.
            .groupBy(
                keySelector = { it.first },      // Group by Muscle
                valueTransform = { it.second }   // Collect only the value lists
            )
            // Calculate the average for each time bucket for each muscle.
            .mapValues { (_, dataLists) ->
                List(numTimeBuckets) { bucketIndex ->
                    // For each bucket, get all non-zero values from the collected lists.
                    val nonZeroValues = dataLists.mapNotNull { dataList ->
                        dataList[bucketIndex].takeIf { it != 0.0 }
                    }

                    // Safely calculate average. .average() on an empty list returns NaN.
                    nonZeroValues.average().takeIf { !it.isNaN() } ?: 0.0
                }
            }
            .map { (muscle, list) ->
                // Each point represent the average performance of a muscle in different time
                // buckets which are defined by cutoffs
                Point(
                    yValues = list,
                    xValue = context.getString(Formatter.exerciseEnumToStringId(muscle)),
                )
            }
    }


    /**
     * Calculates the distribution for each exercise, categorized into dynamic time buckets.
     *
     * This function is highly configurable, allowing for any number of time buckets to be defined
     * via cutoffs and providing an option to include or exclude data that falls outside all specified
     * time periods.
     *
     * @param exerciseDistributionStatisticsChart It defines which value has to be calculated based on
     * the enum passed: [StatisticsChart.LOAD], [StatisticsChart.DURATION] and so on.
     * @param workoutsWithExercises The list of user workouts to process.
     * @param exerciseDistributionCutoffs A list of [LocalDateTime] points that define the boundaries for time buckets.
     *        The function creates `cutoffs.size` buckets for workouts newer than each cutoff, plus
     *        an optional final bucket for all older workouts. The list is automatically sorted internally.
     *        Example: Providing 2 cutoffs creates buckets for workouts newer than `cutoff[0]`,
     *        newer than `cutoff[1]`, and optionally all others (which are older).
     * @param includeOlderWorkouts If `true`, workouts older than all specified cutoffs are aggregated
     * into a final "older" bucket. If `false`, these workouts are entirely excluded from the results.
     * @return A list where each [Point] contains in [Point.yValues] a list of its average performance
     * for each time bucket while in [Point.xValue] there is the name of the associated exercises.
     * The size of the `averages` list corresponds to the number of time buckets generated based on [exerciseDistributionStatisticsChart].
     */
    suspend fun fetchExercisesDistributionWithTimeBuckets(
        exerciseDistributionStatisticsChart: StatisticsChart,
        workoutsWithExercises: List<WorkoutWithExercisesAndSets>,
        exerciseDistributionCutoffs: List<LocalDateTime>,
        includeOlderWorkouts: Boolean = false
    ): List<Point> {
        if (workoutsWithExercises.isEmpty()) {
            return emptyList()
        }

        val sortedCutoffs = exerciseDistributionCutoffs.sortedDescending()
        // The number of buckets depends on whether the final "older" bucket is included.
        val numTimeBuckets =
            if (includeOlderWorkouts) sortedCutoffs.size + 1 else sortedCutoffs.size

        val processedData = coroutineScope {
            // Map each workout to a Deferred result.
            val deferredResults = workoutsWithExercises.map { w ->
                async {
                    // Find the index of the first cutoff the workout date is after.
                    val cutoffIndex = sortedCutoffs.indexOfFirst { w.workout.completed.isAfter(it) }

                    // Determine the final bucket index, or decide to discard the workout.
                    // A `null` index will signify that the workout should be discarded.
                    val timeBucketIndex: Int? = when {
                        // Case 1: The workout falls into one of the defined cutoff buckets.
                        cutoffIndex != -1 -> cutoffIndex
                        // Case 2: It's an older workout, and it is configured to include them.
                        includeOlderWorkouts -> sortedCutoffs.size // This is the last "older" bucket.
                        // Case 3: It's an older workout, and it is configured to EXCLUDE them.
                        else -> null
                    }

                    // If timeBucketIndex is null, return an empty list for this workout.
                    // Otherwise, process the exercises.
                    if (timeBucketIndex == null) {
                        emptyList()
                    } else {
                        // Process these exercises in order to calculate the specified value for each exerciseDC
                        w.exercisesWithSets.mapNotNull { e ->
                            val sets = e.sets.filter { it.completed }
                            val exercise = e.exercise

                            val bodyWeight = measurementRepository.getLastMeasurementByCutoff(
                                w.workout.completed
                            )?.bodyWeight ?: 0.0

                            val value = when (exerciseDistributionStatisticsChart) {
                                StatisticsChart.LOAD -> sets.sumOf {
                                    when (exercise.setMode) {
                                        SetMode.LOAD -> it.load
                                        SetMode.BODYWEIGHT -> bodyWeight
                                        SetMode.BODYWEIGHT_WITH_LOAD -> it.load + bodyWeight
                                        SetMode.DURATION -> 0
                                    }.toDouble()
                                }

                                StatisticsChart.REPS -> sets.sumOf { it.reps }
                                StatisticsChart.VOLUME -> sets.sumOf {
                                    when (exercise.setMode) {
                                        SetMode.LOAD -> it.load
                                        SetMode.BODYWEIGHT -> bodyWeight
                                        SetMode.BODYWEIGHT_WITH_LOAD -> it.load + bodyWeight
                                        SetMode.DURATION -> 0
                                    }.toDouble() * it.reps
                                }

                                StatisticsChart.DURATION -> sets.sumOf { it.elapsedTime }
                            }.toDouble()

                            if (value == 0.0) {
                                null // Filter out zero-value
                            } else {
                                // Create a encoded list for the value's time bucket.
                                val values = MutableList(numTimeBuckets) { 0.0 }.apply {
                                    set(timeBucketIndex, value)
                                }.toList()
                                // Pair the exercise with the calculated value list.
                                e.exerciseDC to values
                            }
                        }
                    }
                }
            }
            // Wait for all parallel jobs to complete while
            // `flatten()` converts the List<List<Pair>> into a single List<Pair>.
            deferredResults.awaitAll().flatten()
        }


        return processedData
            // Group all value lists by exercise.
            .groupBy(
                keySelector = { it.first },      // Group by ExerciseDC
                valueTransform = { it.second }   // Collect only the value lists
            )
            // Calculate the average for each time bucket for each exercise.
            .mapValues { (_, dataLists) ->
                List(numTimeBuckets) { bucketIndex ->
                    // For each bucket, get all non-zero values from the collected lists.
                    val nonZeroValues = dataLists.mapNotNull { dataList ->
                        dataList[bucketIndex].takeIf { it != 0.0 }
                    }

                    // Safely calculate average. .average() on an empty list returns NaN.
                    nonZeroValues.average().takeIf { !it.isNaN() } ?: 0.0
                }
            }
            .map { (exercise, list) ->
                // Each point represent the average performance of an exercise in different time
                // buckets which are defined by cutoffs
                Point(
                    yValues = list,
                    xValue = exercise.name,
                )
            }
    }

    suspend fun fetchPointsForExercisesChart(
        exerciseChart: ExerciseChart,
        workoutsWithExercises: List<WorkoutWithExercisesAndSets>
    ): List<Point> = coroutineScope {
        workoutsWithExercises
            .flatMap { it.exercisesWithSets }
            .map { eWs ->
                async {
                    val workout =
                        workoutsWithExercises.find { eWs in it.exercisesWithSets }?.workout

                    val includeBodyweight = eWs.exercise.setMode == SetMode.BODYWEIGHT ||
                            eWs.exercise.setMode == SetMode.BODYWEIGHT_WITH_LOAD

                    val bodyWeight = if (includeBodyweight && workout != null) measurementRepository
                        .getLastMeasurementByCutoff(workout.completed)?.bodyWeight ?: 0.0 else 0.0

                    val sets = eWs.sets.filter { it.completed }


                    Point(
                        yValues = listOf(
                            when (exerciseChart) {
                                TimeChart.BEST_TIME -> sets.maxOfOrNull { set -> set.elapsedTime }
                                    ?: 0
                                TimeChart.TOTAL_TIME -> sets.sumOf { set -> set.elapsedTime }
                                WeightedBodyweightChart.TOTAL_REPS -> sets.sumOf { set ->
                                    set.reps
                                }
                                WeightedBodyweightChart.TOTAL_VOLUME -> sets.sumOf { set ->
                                    (set.load + bodyWeight) * set.reps.toDouble()
                                }
                                WeightedBodyweightChart.BEST_SET_VOLUME -> sets.maxOfOrNull { set ->
                                    (set.load + bodyWeight) * set.reps
                                } ?: 0

                                WeightedBodyweightChart.HEAVIEST_WEIGHT -> sets.maxOfOrNull { set ->
                                    set.load + bodyWeight
                                } ?: 0

                                BodyweightChart.MOST_REPS -> sets.maxOfOrNull { set -> set.reps }
                                    ?: 0
                                BodyweightChart.SESSION_REPS -> sets.sumOf { set -> set.reps }
                                LoadChart.HEAVIEST_WEIGHT -> sets.maxOfOrNull { set -> set.load }
                                    ?: 0

                                LoadChart.BEST_SET_VOLUME -> sets.maxOfOrNull { set ->
                                    set.load * set.reps
                                } ?: 0

                                LoadChart.ONE_REP_MAX -> sets.maxOfOrNull { set ->
                                    OneRepMaxCalculator.calculate(set.load, set.reps)
                                } ?: 0
                                LoadChart.TOTAL_REPS -> sets.sumOf { set ->
                                    set.reps
                                }
                                LoadChart.SESSION_VOLUME -> sets.sumOf { set ->
                                    set.load * set.reps.toDouble()
                                }
                            }.toDouble()
                        ),
                        xValue = workout?.completed?.let(Formatter::getShortDateFromLocalDate)
                            ?: "",
                        workoutId = workout?.id
                    )
                }
            }
            .awaitAll()

    }
}