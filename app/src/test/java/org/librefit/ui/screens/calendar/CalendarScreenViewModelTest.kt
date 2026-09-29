/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.calendar

import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.librefit.db.repository.WorkoutRepository
import org.librefit.ui.models.UiWorkout
import org.librefit.ui.models.mappers.toEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalMaterial3Api::class)
class CalendarScreenViewModelTest {
    // The mock repository
    private lateinit var workoutRepository: WorkoutRepository

    // The class under test
    private lateinit var viewModel: CalendarScreenViewModel

    // A controllable flow to simulate repository emissions
    private lateinit var completedWorkoutsFlow: MutableStateFlow<List<UiWorkout>>

    // Test data
    private val date1morning = LocalDateTime.of(2025, 7, 29, 9, 0)
    private val date1evening = LocalDateTime.of(2025, 7, 29, 19, 0)

    private val workout1 = UiWorkout(id = 1, title = "Morning Run", completed = date1morning)
    private val workout2 = UiWorkout(id = 2, title = "Evening Gym", completed = date1evening)

    private val date2morning = LocalDateTime.of(2025, 7, 30, 9, 0)
    private val workout3 = UiWorkout(id = 3, title = "Cycling", completed = date2morning)

    private val allWorkouts = listOf(workout1, workout2, workout3)


    @BeforeTest
    fun setUp() {
        // Arrange: Create a mock for the repository
        workoutRepository = mockk()
        completedWorkoutsFlow = MutableStateFlow(emptyList())

        // Arrange: Tell the mock what to return when `completedWorkouts` is accessed
        every { workoutRepository.completedWorkouts } returns completedWorkoutsFlow.map { f -> f.map { it.toEntity() } }

        // Arrange: Create the ViewModel instance with the mock repository
        viewModel = CalendarScreenViewModel(workoutRepository)
    }

    @Test
    fun `initial state - workoutsFromDate is empty when no date is selected`() = runTest {
        // Assert
        assertTrue(viewModel.workoutsFromDate.value.isEmpty())
    }

    @Test
    fun `initial state - year range has no bounds`() = runTest {
        assertEquals(DatePickerDefaults.YearRange, viewModel.yearRange.value)
    }

    @Test
    fun `initial state - all dates are selectable`() = runTest {
        assertEquals(DatePickerDefaults.AllDates, viewModel.selectableDates.value)
    }

    @Test
    fun `when date is selected - workoutsFromDate emits filtered list for that date`() = runTest {
        // Arrange: Provide workouts from the repository
        completedWorkoutsFlow.value = allWorkouts
        val date1InMillis =
            date2morning.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        // Use Turbine to test the flow
        viewModel.workoutsFromDate.test {
            // The initial emission is always empty because date is null
            assertTrue(awaitItem().isEmpty())

            // Act: Update the selected date
            viewModel.updateSelectedDateInMillis(date1InMillis)

            // Assert: The flow should emit the list of workouts filtered for the selected date
            val expectedWorkouts = listOf(workout3)
            assertEquals(expectedWorkouts, awaitItem())
        }
    }

    @Test
    fun `when date is changed from one with workouts to one without - workoutsFromDate emits empty list`() =
        runTest {
            // Arrange: Provide workouts from the repository
            completedWorkoutsFlow.value = allWorkouts
            val dateWithWorkoutsInMillis =
                date1morning.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            val dateWithNoWorkoutsInMillis =
                LocalDate.of(2025, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

            viewModel.workoutsFromDate.test {
                // Consume the initial empty list (because no date is selected yet)
                assertTrue(awaitItem().isEmpty())

                // Act: Select a date that *has* workouts to establish a non-empty state
                viewModel.updateSelectedDateInMillis(dateWithWorkoutsInMillis)

                // Assert & Consume: Confirm the workouts are received. The state is now non-empty.
                val expectedWorkouts = listOf(workout1, workout2)
                assertEquals(expectedWorkouts, awaitItem())

                // Act: Now, select the date with *no* workouts. This is the real action to test.
                viewModel.updateSelectedDateInMillis(dateWithNoWorkoutsInMillis)

                // Assert: The flow should now transition from the list of workouts to an empty list.
                assertTrue(awaitItem().isEmpty())
            }
        }

    @Test
    fun `when selected date is cleared - workoutsFromDate emits empty list`() = runTest {
        // Arrange
        completedWorkoutsFlow.value = allWorkouts
        val date1InMillis =
            date1morning.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        viewModel.workoutsFromDate.test {
            assertTrue(awaitItem().isEmpty()) // Initial state

            // Select a date first
            viewModel.updateSelectedDateInMillis(date1InMillis)

            // Consume the emission for the selected date
            awaitItem()

            // Act: Clear the selected date
            viewModel.updateSelectedDateInMillis(null)

            // Assert: The flow should emit an empty list again
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun `when repository emits new workouts - workoutsFromDate updates for selected date`() =
        runTest {
            // Arrange: Select a date first
            val date1InMillis =
                date1morning.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            viewModel.updateSelectedDateInMillis(date1InMillis)

            viewModel.workoutsFromDate.test {
                // Initial emission is empty as repo is empty
                assertTrue(awaitItem().isEmpty())

                // Act: Repository emits the first list of workouts
                completedWorkoutsFlow.value = allWorkouts

                // Assert: The flow updates with filtered workouts
                val expectedWorkouts1 = listOf(workout1, workout2)
                assertEquals(expectedWorkouts1, awaitItem())

                // Act: Repository emits an updated list (e.g., a new workout was completed)
                val newWorkout = UiWorkout(id = 4, title = "Late walk", completed = date1morning)
                completedWorkoutsFlow.value = allWorkouts + newWorkout

                // Assert: The flow updates again with the new filtered list
                val expectedWorkouts2 = listOf(workout1, workout2, newWorkout)
                assertEquals(expectedWorkouts2, awaitItem())
            }
        }

    @Test
    fun `when workout list updates - year range reflect the max and min years`() = runTest {
        viewModel.yearRange.test {
            // Initial emission
            assertEquals(DatePickerDefaults.YearRange, awaitItem())

            // Act: Provide workouts from the repository
            completedWorkoutsFlow.value = allWorkouts + UiWorkout(
                completed = LocalDateTime.of(2023, 1, 1, 1, 1)
            )

            // Assert: year range reflects the max and min years in the provided workouts
            assertEquals((2023..2025), awaitItem())
        }
    }
}
