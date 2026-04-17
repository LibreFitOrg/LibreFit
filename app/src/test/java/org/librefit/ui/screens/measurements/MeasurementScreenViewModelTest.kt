/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.measurements

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.librefit.MainDispatcherRule
import org.librefit.db.entity.Measurement
import org.librefit.db.repository.MeasurementRepository
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.enums.MeasurementCardState
import org.librefit.enums.chart.MeasurementChart
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class MeasurementScreenViewModelTest {
    // MainDispatcherRule to control coroutine execution
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // The mock repository
    private lateinit var measurementRepository: MeasurementRepository

    private lateinit var userPreferencesRepository: UserPreferencesRepository

    // The class under test
    private lateinit var viewModel: MeasurementScreenViewModel

    // A controllable flow to simulate repository emissions
    private lateinit var measurementsFlow: MutableStateFlow<List<Measurement>>

    private lateinit var useNumberPicker: MutableStateFlow<Boolean>

    // Captured objects
    private val upsertedMeasurementSlot = slot<Measurement>()
    private val idMeasurementToDelete = slot<Long>()

    // Test data
    private val now: LocalDateTime = LocalDateTime.now()

    private val allMeasurements = listOf(
        Measurement(id = 1, bodyWeight = 90.0, bodyFatPercentage = 10),
        Measurement(id = 2, bodyWeight = 88.0, muscleMassPercentage = 20, date = now.minusDays(7)),
        Measurement(id = 3, bodyWeight = 93.0, notes = "notes", date = now.minusDays(14))
    )

    @Before
    fun setUp() {
        // Arrange: Create a mock for the repository
        measurementRepository = mockk()
        measurementsFlow = MutableStateFlow(emptyList())
        useNumberPicker = MutableStateFlow(true)

        userPreferencesRepository = mockk()

        // Arrange: Tell the mock what to return when `measurements` is accessed
        every { measurementRepository.measurements } returns measurementsFlow

        // Arrange: Set up the mock repository
        coEvery { measurementRepository.upsertMeasurement(capture(upsertedMeasurementSlot)) } answers {
            val upsertedItem = upsertedMeasurementSlot.captured
            val currentList = measurementsFlow.value.toMutableList()

            val existingIndex = currentList.indexOfFirst { it.id == upsertedItem.id }

            if (existingIndex != -1) {
                currentList[existingIndex] = upsertedItem
            } else {
                currentList.add(upsertedItem)
            }
            measurementsFlow.value = currentList
        }
        coEvery { measurementRepository.deleteById(capture(idMeasurementToDelete)) } answers {
            val updatedList =
                measurementsFlow.value.filter { it.id != idMeasurementToDelete.captured }
            measurementsFlow.value = updatedList
        }

        every { userPreferencesRepository.useNumberPicker } returns useNumberPicker


        viewModel = MeasurementScreenViewModel(
            measurementRepository,
            defaultDispatcher = mainDispatcherRule.testDispatcher,
            userPreferencesRepository = userPreferencesRepository
        )
    }

    @Test
    fun `initial state - measurements list is empty `() = runTest {
        assertThat(viewModel.measurements.value).isEmpty()
    }

    @Test
    fun `initial state - points list is empty `() = runTest {
        assertThat(viewModel.points.value).isEmpty()
    }

    @Test
    fun `initial state - measurement chart is set to bodyweight `() = runTest {
        assertThat(viewModel.measurementChart.value).isEqualTo(MeasurementChart.BODY_WEIGHT)
    }

    @Test
    fun `initial state - id measurement is 0 `() = runTest {
        assertThat(viewModel.idMeasurement.value).isEqualTo(0L)
    }

    @Test
    fun `initial state - body weight is empty `() = runTest {
        assertThat(viewModel.bodyWeight.value).isEmpty()
    }

    @Test
    fun `initial state - fat mass is null `() = runTest {
        assertThat(viewModel.fatMass.value).isNull()
    }

    @Test
    fun `initial state - lean mass is null `() = runTest {
        assertThat(viewModel.leanMass.value).isNull()
    }

    @Test
    fun `initial state - notes are empty `() = runTest {
        assertThat(viewModel.notes.value).isEmpty()
    }

    @Test
    fun `initial state - measurement card state is new `() = runTest {
        assertThat(viewModel.measurementCardState.value).isEqualTo(MeasurementCardState.NEW)
    }

    @Test
    fun `when measurements are loaded - points list contains the bodyweight of all measurements`() =
        runTest {
            // Arrange: Provide measurements from the repository
            measurementsFlow.value = allMeasurements

            viewModel.points.test {

                // Assert: The flow should emit list of `ChartData` having yValue equal to the respective bodyweight
                val actual = awaitItem().map { it.yValues.first() }
                val expected = allMeasurements.map { it.bodyWeight }

                assertThat(actual).isEqualTo(expected)
            }
        }

    @Test
    fun `when measurements are updated - points list the bodyweight of all measurements`() =
        runTest {
            // Arrange: Provide measurements from the repository
            measurementsFlow.value = allMeasurements

            viewModel.points.test {

                // Assert: The flow should emit list of `ChartData` having yValue equal to the respective bodyweight
                var actual = awaitItem().map { it.yValues.first() }
                var expected = allMeasurements.map { it.bodyWeight }

                assertThat(actual).isEqualTo(expected)


                // Act: update measurements
                val newMeasurements = allMeasurements + Measurement(id = 4, bodyWeight = 90.0)
                measurementsFlow.value = newMeasurements

                // Assert: Check list of `ChartData` again
                actual = awaitItem().map { it.yValues.first() }
                expected = newMeasurements.map { it.bodyWeight }

                assertThat(actual).isEqualTo(expected)
            }
        }

    @Test
    fun `when measurement chart is updated - points list the respective chart values`() =
        runTest {
            // Arrange: Provide measurements from the repository
            measurementsFlow.value = allMeasurements

            // Act: update measurement chart
            viewModel.updateMeasurementChart(MeasurementChart.FAT_MASS)

            viewModel.points.test {

                // Assert: The flow should emit list of `ChartData` having yValue equal to the respective fat mass
                val actual = awaitItem().map { it.yValues.first() }
                val expected =
                    allMeasurements.map { it.bodyFatPercentage.toDouble() }.filter { it != 0.0 }

                assertThat(actual).isEqualTo(expected)
            }
        }

    @Test
    fun `when inserting a new measurement - measurements flow should emit an updated list`() =
        runTest {
            // Arrange: Define the initial and expected states
            val newNotes = "This is a new measurement"
            val insertedMeasurement = Measurement(notes = newNotes, bodyWeight = 90.3)
            val updatedMeasurements = allMeasurements + insertedMeasurement

            // Arrange: Set the initial value for the flow.
            measurementsFlow.value = allMeasurements


            viewModel.measurements.test {
                // Assert: The first item emitted should be the initial list of measurements.
                assertThat(awaitItem()).isEqualTo(allMeasurements)

                // Act: Simulate the user entering data and saving a new measurement.
                viewModel.updateNotes(insertedMeasurement.notes)
                viewModel.updateDate(insertedMeasurement.date)
                viewModel.updateBodyweight(insertedMeasurement.bodyWeight.toString())
                viewModel.upsertMeasurementToDB()

                // Assert: Await the new emission and verify its contents are correct
                assertThat(awaitItem()).isEqualTo(updatedMeasurements)

                // Assert: Verify the captured object
                assertThat(upsertedMeasurementSlot.captured).isEqualTo(insertedMeasurement)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when updating a measurement - measurements flow should emit an updated list`() = runTest {
        // Arrange: Define the initial and expected states
        val newNotes = "This is a edited measurement"
        val updatedMeasurement = allMeasurements.random().copy(notes = newNotes)
        val updatedMeasurements = allMeasurements.map {
            if (it.id == updatedMeasurement.id) updatedMeasurement else it
        }

        // Arrange: Set the initial value for the flow.
        measurementsFlow.value = allMeasurements


        viewModel.measurements.test {
            // Assert: The first item emitted should be the initial list of measurements.
            assertThat(awaitItem()).isEqualTo(allMeasurements)

            // Act: Simulate the user entering data and save the measurement
            viewModel.updateMeasurementCardState(MeasurementCardState.EDIT)
            viewModel.updateIdMeasurement(updatedMeasurement.id)
            viewModel.updateBodyweight(updatedMeasurement.bodyWeight.toString())
            viewModel.updateFatMass(updatedMeasurement.bodyFatPercentage.toString())
            viewModel.updateLeanMass(updatedMeasurement.muscleMassPercentage.toString())
            viewModel.updateDate(updatedMeasurement.date)
            viewModel.updateNotes(updatedMeasurement.notes)
            viewModel.upsertMeasurementToDB()

            // Assert: Verify the captured object
            assertThat(upsertedMeasurementSlot.captured).isEqualTo(updatedMeasurement)

            // Assert: Await the new emission and verify its contents are correct
            assertThat(awaitItem()).isEqualTo(updatedMeasurements)
        }
    }


    @Test
    fun `when updating a measurement without measurement card state as NEW - measurements flow should NOT emit the right updated list`() =
        runTest {
            // Arrange: Define the initial and expected states
            val newNotes = "This is a edited measurement"
            val updatedMeasurement = allMeasurements.random().copy(notes = newNotes)
            val updatedMeasurements = allMeasurements.map {
                if (it.id == updatedMeasurement.id) updatedMeasurement else it
            }

            // Arrange: Set the initial value for the flow.
            measurementsFlow.value = allMeasurements


            viewModel.measurements.test {
                // Assert: The first item emitted should be the initial list of measurements.
                assertThat(awaitItem()).isEqualTo(allMeasurements)

                // Act: Simulate the user entering data and saving a new measurement (instead of updating it).
                viewModel.updateMeasurementCardState(MeasurementCardState.NEW)
                viewModel.updateIdMeasurement(updatedMeasurement.id)
                viewModel.updateBodyweight(updatedMeasurement.bodyWeight.toString())
                viewModel.updateFatMass(updatedMeasurement.bodyFatPercentage.toString())
                viewModel.updateLeanMass(updatedMeasurement.muscleMassPercentage.toString())
                viewModel.updateDate(updatedMeasurement.date)
                viewModel.updateNotes(updatedMeasurement.notes)
                viewModel.upsertMeasurementToDB()

                // Assert: Verify the captured object
                assertThat(upsertedMeasurementSlot.captured).isNotEqualTo(updatedMeasurement)

                // Assert: Await the new emission and verify its contents are correct
                assertThat(awaitItem()).isNotEqualTo(updatedMeasurements)
            }
        }

    @Test
    fun `when deleting a measurement - measurements flow should emit an updated list`() = runTest {
        // Arrange: Define the initial and expected states
        val measurementToDelete = allMeasurements.random()
        val expectedId = measurementToDelete.id
        val updatedMeasurements = allMeasurements.filter { it != measurementToDelete }

        // Arrange: Set the initial value for the flow.
        measurementsFlow.value = allMeasurements


        viewModel.measurements.test {

            // Assert: The first item emitted should be the initial list of measurements.
            assertThat(awaitItem()).isEqualTo(allMeasurements)

            // Act: Delete measurement by id
            viewModel.deleteMeasurementById(expectedId)

            // Assert: Verify the captured object
            assertThat(idMeasurementToDelete.captured).isEqualTo(expectedId)

            // Assert: Await the new emission and verify its contents are correct
            assertThat(awaitItem()).isEqualTo(updatedMeasurements)

        }
    }

    @Test
    fun `when deleting with invalid ID - measurements flow should not emit a new list`() = runTest {
        // Arrange: Define an ID that does not exist in the list.
        val invalidId = -1L

        // Arrange: Set the initial value for the flow.
        measurementsFlow.value = allMeasurements


        viewModel.measurements.test {
            // Assert: The first item emitted should be the initial list.
            assertThat(awaitItem()).isEqualTo(allMeasurements)

            // Act: Attempt to delete a measurement with the non-existent ID.
            viewModel.deleteMeasurementById(invalidId)

            // Assert: NO new measurement is emitted by the flow (because the id in invalid and nothing changes)
            expectNoEvents()
        }
    }
}