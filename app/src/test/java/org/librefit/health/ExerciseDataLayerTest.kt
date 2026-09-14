/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.health

import androidx.health.connect.client.records.ExerciseSegment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.librefit.db.entity.Exercise
import org.librefit.db.entity.ExerciseDC
import org.librefit.db.entity.Set
import org.librefit.db.entity.Workout
import org.librefit.db.relations.ExerciseWithSets
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.enums.healthConnect.HealthConnectSegmentType
import java.time.LocalDateTime
import java.time.ZoneId

class ExerciseDataLayerTest {
    private val dataLayer = ExerciseDataLayer()

    @Test
    fun `segment mapping uses the explicit exercise type`() {
        assertEquals(
            ExerciseSegment.EXERCISE_SEGMENT_TYPE_LEG_CURL,
            HealthConnectSegmentType.LEG_CURL.exerciseSegmentType
        )
        assertEquals(
            ExerciseSegment.EXERCISE_SEGMENT_TYPE_DUMBBELL_ROW,
            HealthConnectSegmentType.DUMBBELL_ROW.exerciseSegmentType
        )
    }

    @Test
    fun `custom exercises default to other workout`() {
        val customExercise = ExerciseDC(isCustomExercise = true)

        assertEquals(
            HealthConnectSegmentType.OTHER_WORKOUT,
            customExercise.healthConnectSegmentType
        )
    }

    @Test
    fun `workout without complete set timestamps is not exported`() {
        val workout = workoutWithSet(Set(completed = true, reps = 10))

        assertNull(dataLayer.toExerciseSessionRecord(workout))
    }

    @Test
    fun `workout with complete set timestamps uses the recorded times`() {
        val start = LocalDateTime.of(2026, 7, 23, 10, 0)
        val end = start.plusSeconds(30)
        val workout = workoutWithSet(
            Set(
                completed = true,
                reps = 10,
                startedAt = start,
                completedAt = end
            )
        )

        val record = dataLayer.toExerciseSessionRecord(workout)

        assertNotNull(record)
        assertEquals(start.atZone(ZoneId.systemDefault()).toInstant(), record?.startTime)
        assertEquals(end.atZone(ZoneId.systemDefault()).toInstant(), record?.endTime)
    }

    private fun workoutWithSet(set: Set): WorkoutWithExercisesAndSets {
        val completed = set.completedAt ?: LocalDateTime.of(2026, 7, 23, 10, 1)
        return WorkoutWithExercisesAndSets(
            workout = Workout(id = 1, completed = completed),
            exercisesWithSets = listOf(
                ExerciseWithSets(
                    exercise = Exercise(id = 1),
                    sets = listOf(set),
                    exerciseDC = ExerciseDC(
                        name = "Squat",
                        healthConnectSegmentType = HealthConnectSegmentType.SQUAT
                    )
                )
            )
        )
    }
}
