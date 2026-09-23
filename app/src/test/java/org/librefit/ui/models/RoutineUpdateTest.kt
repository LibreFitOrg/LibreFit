/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.models

import kotlinx.collections.immutable.persistentListOf
import org.librefit.enums.SetMode
import org.librefit.models.Weight
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class RoutineUpdateTest {

    private val benchPress = UiExerciseDC(id = "Barbell_Bench_Press_-_Medium_Grip")
    private val running = UiExerciseDC(id = "Running_Treadmill")
    private val squat = UiExerciseDC(id = "Barbell_Squat")

    @Test
    fun `withValuesOfCompletedSets takes values of completed sets only`() {
        val routine = listOf(
            UiExerciseWithSets(
                exercise = UiExercise(id = 11L, setMode = SetMode.LOAD),
                sets = persistentListOf(
                    UiSet(id = 1L, load = Weight.kilograms(50.0), reps = 8),
                    UiSet(id = 2L, load = Weight.kilograms(50.0), reps = 8),
                    UiSet(id = 3L, load = Weight.kilograms(50.0), reps = 8)
                ),
                exerciseDC = benchPress
            )
        )
        val workout = listOf(
            UiExerciseWithSets(
                exercise = UiExercise(id = 99L, setMode = SetMode.LOAD),
                sets = persistentListOf(
                    UiSet(load = Weight.kilograms(55.0), reps = 6, completed = true),
                    UiSet(load = Weight.kilograms(60.0), reps = 5)
                ),
                exerciseDC = benchPress
            )
        )

        val updated = routine.withValuesOfCompletedSets(workout).single()

        assertEquals(11L, updated.exercise.id)
        assertContentEquals(listOf(1L, 2L, 3L), updated.sets.map { it.id })
        assertContentEquals(
            listOf(Weight.kilograms(55.0), Weight.kilograms(50.0), Weight.kilograms(50.0)),
            updated.sets.map { it.load }
        )
        assertContentEquals(listOf(6, 8, 8), updated.sets.map { it.reps })
        assertContentEquals(listOf(false, false, false), updated.sets.map { it.completed })
    }

    @Test
    fun `withValuesOfCompletedSets keeps the number of sets of the routine`() {
        val routine = listOf(
            UiExerciseWithSets(
                exercise = UiExercise(setMode = SetMode.BODYWEIGHT),
                sets = persistentListOf(UiSet(reps = 10)),
                exerciseDC = benchPress
            )
        )
        val workout = listOf(
            UiExerciseWithSets(
                exercise = UiExercise(setMode = SetMode.BODYWEIGHT),
                sets = persistentListOf(
                    UiSet(reps = 12, completed = true),
                    UiSet(reps = 11, completed = true)
                ),
                exerciseDC = benchPress
            )
        )

        val updated = routine.withValuesOfCompletedSets(workout).single()

        assertContentEquals(listOf(12), updated.sets.map { it.reps })
    }

    @Test
    fun `withValuesOfCompletedSets ignores exercises whose type of set has been changed`() {
        val routine = listOf(
            UiExerciseWithSets(
                exercise = UiExercise(setMode = SetMode.LOAD),
                sets = persistentListOf(UiSet(load = Weight.kilograms(50.0), reps = 8)),
                exerciseDC = benchPress
            )
        )
        val workout = listOf(
            UiExerciseWithSets(
                exercise = UiExercise(setMode = SetMode.BODYWEIGHT),
                sets = persistentListOf(UiSet(reps = 12, completed = true)),
                exerciseDC = benchPress
            )
        )

        assertEquals(routine, routine.withValuesOfCompletedSets(workout))
    }

    @Test
    fun `withValuesOfCompletedSets ignores exercises not in the workout`() {
        val routine = listOf(
            UiExerciseWithSets(
                exercise = UiExercise(setMode = SetMode.LOAD),
                sets = persistentListOf(UiSet(load = Weight.kilograms(50.0), reps = 8)),
                exerciseDC = benchPress
            )
        )
        val workout = listOf(
            UiExerciseWithSets(
                exercise = UiExercise(setMode = SetMode.LOAD),
                sets = persistentListOf(
                    UiSet(load = Weight.kilograms(100.0), reps = 5, completed = true)
                ),
                exerciseDC = squat
            )
        )

        assertEquals(routine, routine.withValuesOfCompletedSets(workout))
    }

    @Test
    fun `withValuesOfCompletedSets matches repeated exercises in order`() {
        val routine = listOf(
            UiExerciseWithSets(
                exercise = UiExercise(setMode = SetMode.DURATION),
                sets = persistentListOf(UiSet(elapsedTime = 60)),
                exerciseDC = running
            ),
            UiExerciseWithSets(
                exercise = UiExercise(setMode = SetMode.DURATION),
                sets = persistentListOf(UiSet(elapsedTime = 60)),
                exerciseDC = running
            )
        )
        val workout = listOf(
            UiExerciseWithSets(
                exercise = UiExercise(setMode = SetMode.DURATION),
                sets = persistentListOf(UiSet(elapsedTime = 300, completed = true)),
                exerciseDC = running
            ),
            UiExerciseWithSets(
                exercise = UiExercise(setMode = SetMode.DURATION),
                sets = persistentListOf(UiSet(elapsedTime = 120, completed = true)),
                exerciseDC = running
            )
        )

        val updated = routine.withValuesOfCompletedSets(workout)

        assertContentEquals(listOf(300, 120), updated.map { it.sets.single().elapsedTime })
    }
}
