/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.db.repository

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.librefit.db.dao.WorkoutDao
import org.librefit.db.entity.Workout
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.enums.WorkoutState
import org.librefit.health.HealthConnectRepository

class WorkoutRepositoryTest {
    private lateinit var workoutDao: WorkoutDao
    private lateinit var userPreferences: UserPreferencesRepository
    private lateinit var healthConnectRepository: HealthConnectRepository
    private lateinit var repository: WorkoutRepository

    @Before
    fun setUp() {
        workoutDao = mockk()
        userPreferences = mockk()
        healthConnectRepository = mockk()

        every { workoutDao.getWorkoutsByStateAndOrderedByCompleted(any()) } returns
            MutableStateFlow(emptyList())
        every { workoutDao.getWorkoutsByState(any()) } returns MutableStateFlow(emptyList())
        every {
            workoutDao.getWorkoutsWithExercisesAndSetsByStateAndOrderedByCompleted(any())
        } returns MutableStateFlow(emptyList())
        every { workoutDao.getWorkoutsWithExercisesAndSetsByState(any()) } returns
            MutableStateFlow(emptyList())
        every { userPreferences.healthConnectEnabled } returns MutableStateFlow(false)
        coEvery { userPreferences.isHealthConnectEnabled() } returns false
        coEvery { userPreferences.isHealthConnectSyncEnabled(any()) } returns false
        repository = WorkoutRepository(
            workoutDao,
            userPreferences,
            healthConnectRepository
        )
    }

    @Test
    fun `new workout receives a health connect version`() = runTest {
        val savedWorkout = slot<WorkoutWithExercisesAndSets>()
        coEvery { workoutDao.addWorkoutWithExercisesAndSets(capture(savedWorkout)) } returns 4L

        repository.addWorkoutWithExercisesAndSets(
            WorkoutWithExercisesAndSets(Workout(), emptyList())
        )

        assertThat(savedWorkout.captured.workout.healthConnectRecordVersion).isGreaterThan(0L)
    }

    @Test
    fun `edited workout always receives a newer health connect version`() = runTest {
        val previousVersion = System.currentTimeMillis() + 10_000
        val workout = Workout(
            id = 5,
            state = WorkoutState.COMPLETED,
            healthConnectRecordVersion = previousVersion
        )
        val savedWorkout = slot<WorkoutWithExercisesAndSets>()
        coEvery { workoutDao.getWorkout(5) } returns workout
        coEvery { workoutDao.addWorkoutWithExercisesAndSets(capture(savedWorkout)) } returns 5L

        repository.addWorkoutWithExercisesAndSets(
            WorkoutWithExercisesAndSets(workout, emptyList())
        )

        assertThat(savedWorkout.captured.workout.healthConnectRecordVersion)
            .isEqualTo(previousVersion + 1)
    }
}
