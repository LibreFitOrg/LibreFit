/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.health

import androidx.health.connect.client.records.ExerciseSegment
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.metadata.Metadata
import org.librefit.db.entity.Set
import org.librefit.db.relations.ExerciseWithSets
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.enums.exercise.Category
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseDataLayer @Inject constructor() {
    fun toExerciseSessionRecord(
        workoutWithExercisesAndSets: WorkoutWithExercisesAndSets
    ): ExerciseSessionRecord? {
        val sortedExercises = workoutWithExercisesAndSets.exercisesWithSets.sortedBy {
            it.exercise.position
        }
        val completedSets = sortedExercises.flatMap { exerciseWithSets ->
            exerciseWithSets.sets
                .filter { it.completed }
                .map { set -> exerciseWithSets to set }
        }

        // Older workouts do not have reliable set timestamps and must not be guessed for export.
        if (completedSets.isEmpty() || completedSets.any { (_, set) -> !set.hasValidTimestamps() }) {
            return null
        }

        val zone = ZoneId.systemDefault()
        val sessionType = workoutWithExercisesAndSets.toSessionType()
        val segments = completedSets.map { (exerciseWithSets, set) ->
            ExerciseSegment(
                checkNotNull(set.startedAt).atZone(zone).toInstant(),
                checkNotNull(set.completedAt).atZone(zone).toInstant(),
                exerciseWithSets.toSegmentType(),
                set.reps.coerceAtLeast(0)
            )
        }

        val workout = workoutWithExercisesAndSets.workout
        val startTime = segments.minOf { it.startTime }
        val endTime = maxOf(
            workout.completed.atZone(zone).toInstant(),
            segments.maxOf { it.endTime }
        )
        val startOffset = zone.rules.getOffset(startTime)
        val endOffset = zone.rules.getOffset(endTime)

        return ExerciseSessionRecord(
            startTime = startTime,
            startZoneOffset = startOffset,
            endTime = endTime,
            endZoneOffset = endOffset,
            metadata = Metadata.manualEntry(
                clientRecordId = "librefit-workout-${workout.id}",
                clientRecordVersion = workout.healthConnectRecordVersion
            ),
            exerciseType = sessionType,
            title = workout.title.ifBlank { null },
            notes = workout.toHealthConnectNotes(sortedExercises),
            segments = segments,
            laps = emptyList(),
            plannedExerciseSessionId = null
        )
    }

    private fun ExerciseWithSets.toSegmentType(): Int {
        return exerciseDC.healthConnectSegmentType.exerciseSegmentType
    }

    private fun Set.hasValidTimestamps(): Boolean {
        val start = startedAt ?: return false
        val end = completedAt ?: return false
        return end.isAfter(start)
    }

    private fun WorkoutWithExercisesAndSets.toSessionType(): Int {
        val categories = exercisesWithSets.map { it.exerciseDC.category }.toSet()

        return when {
            categories.isNotEmpty() && categories.all { it == Category.STRETCHING } -> {
                ExerciseSessionRecord.EXERCISE_TYPE_STRETCHING
            }
            categories.isNotEmpty() && categories.all { it == Category.CARDIO } -> {
                ExerciseSessionRecord.EXERCISE_TYPE_OTHER_WORKOUT
            }
            categories.isNotEmpty() && categories.all { it == Category.OLYMPIC_WEIGHTLIFTING } -> {
                ExerciseSessionRecord.EXERCISE_TYPE_WEIGHTLIFTING
            }
            categories.isNotEmpty() && categories.all { it != Category.CARDIO && it != Category.STRETCHING } -> {
                ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING
            }
            else -> ExerciseSessionRecord.EXERCISE_TYPE_OTHER_WORKOUT
        }
    }

    private fun org.librefit.db.entity.Workout.toHealthConnectNotes(
        exercisesWithSets: List<ExerciseWithSets>
    ): String? {
        val exerciseSummary = exercisesWithSets
            .filter { exerciseWithSets -> exerciseWithSets.sets.any { it.completed } }
            .joinToString(separator = "\n") { exerciseWithSets ->
                val sets = exerciseWithSets.sets.count { it.completed }
                "- ${exerciseWithSets.exerciseDC.name}: $sets sets"
            }

        return listOf(notes.takeIf { it.isNotBlank() }, exerciseSummary.takeIf { it.isNotBlank() })
            .filterNotNull()
            .joinToString(separator = "\n\n")
            .ifBlank { null }
    }
}
