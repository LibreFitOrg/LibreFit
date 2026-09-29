/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.exercises

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.librefit.MainDispatcherRule
import org.librefit.db.repository.DatasetRepository
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.enums.exercise.FilterValue
import org.librefit.enums.exercise.Force
import org.librefit.ui.models.UiExerciseDC
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class ExercisesScreenViewModelTest {

    // MainDispatcherRule to control coroutine execution
    private val mainDispatcherRule = MainDispatcherRule()

    // The mock repository
    private lateinit var datasetRepository: DatasetRepository

    private lateinit var userPreferencesRepository: UserPreferencesRepository

    // A controllable flow to simulate repository emissions
    private lateinit var datasetFlow: MutableStateFlow<List<UiExerciseDC>>

    private lateinit var isSupporterFlow: MutableStateFlow<Boolean>

    private lateinit var showExercisesImages: MutableStateFlow<Boolean?>

    // Test dataset
    private val dataset = listOf(
        UiExerciseDC(name = "Pull exercise", force = Force.PULL),
        UiExerciseDC(name = "Push exercise", force = Force.PUSH),
        UiExerciseDC(name = "Exercise", force = Force.PULL)
    )

    private lateinit var viewModel: ExercisesScreenViewModel

    @BeforeTest
    fun setUpMainDispatcher() {
        mainDispatcherRule.setUp()
    }

    @AfterTest
    fun tearDownMainDispatcher() {
        mainDispatcherRule.tearDown()
    }

    @BeforeTest
    fun setUp() {
        // Arrange: Create a mock for the repository
        datasetRepository = mockk()
        datasetFlow = MutableStateFlow(dataset)

        userPreferencesRepository = mockk()
        isSupporterFlow = MutableStateFlow(false)
        showExercisesImages = MutableStateFlow(null)

        // Arrange: Tell the mock what to return when a variable is accessed
        every { datasetRepository.dataset } returns datasetFlow
        every { userPreferencesRepository.isSupporter } returns isSupporterFlow
        every { userPreferencesRepository.showExercisesImages } returns showExercisesImages

        // Instantiate the ViewModel directly, passing in test data
        viewModel = ExercisesScreenViewModel(
            datasetRepository = datasetRepository,
            userPreferencesRepository = userPreferencesRepository
        )
    }

    @Test
    fun `initial state - query is empty`() = runTest {
        assertTrue(viewModel.query.value.isEmpty())
    }

    @Test
    fun `initial state - debounced query is empty`() = runTest {
        viewModel.debouncedQuery.test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun `initial state - filter is empty`() = runTest {
        assertEquals(FilterValue(), viewModel.filterValue.value)
    }

    @Test
    fun `initial state - filtered exercise list is equal to dataset`() = runTest {
        assertEquals(dataset, viewModel.filteredExerciseList.value)
    }

    @Test
    fun `updateQuery updates the query state flow`() = runTest {
        val query = "query"
        // Arrange: The query is updated
        viewModel.updateQuery(query)

        // Assert: The immediate query state is updated
        assertEquals(query, viewModel.query.value)
    }

    @Test
    fun `filteredExerciseList updates after query debounce period`() = runTest(
        context = mainDispatcherRule.testDispatcher
    ) {
        viewModel.filteredExerciseList.test {
            // Assert: The initial item is the full list
            assertEquals(dataset, awaitItem())

            // Arrange: The query is updated
            viewModel.updateQuery("Exercise")

            // Arrange: Advance the virtual clock past the debounce timeout
            mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(301L)

            // Assert: The new, filtered list is ordered by fuzzySearch
            val filteredList = awaitItem()
            assertContentEquals(
                listOf(
                    UiExerciseDC(name = "Exercise", force = Force.PULL),
                    UiExerciseDC(name = "Pull exercise", force = Force.PULL),
                    UiExerciseDC(name = "Push exercise", force = Force.PUSH)
                ),
                filteredList
            )
        }
    }

    @Test
    fun `filteredExerciseList updates after last query debounce period`() = runTest(
        context = mainDispatcherRule.testDispatcher
    ) {
        viewModel.filteredExerciseList.test {
            // Assert: The initial item is the full list
            assertEquals(dataset, awaitItem())

            // Arrange: The query is updated multiple times and advance the virtual clock past the debounce timeout
            viewModel.updateQuery("Exe")
            mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(100L)

            viewModel.updateQuery("Exerc")
            mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(100L)

            viewModel.updateQuery("Exercise")
            mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(301L)

            // Assert: The new, filtered list is ordered by fuzzySearch
            val filteredList = awaitItem()
            assertContentEquals(
                listOf(
                    UiExerciseDC(name = "Exercise", force = Force.PULL),
                    UiExerciseDC(name = "Pull exercise", force = Force.PULL),
                    UiExerciseDC(name = "Push exercise", force = Force.PUSH)
                ),
                filteredList
            )
        }
    }

    @Test
    fun `filteredExerciseList doesn't update before query debounce period`() = runTest(
        context = mainDispatcherRule.testDispatcher
    ) {
        viewModel.filteredExerciseList.test {
            // Assert: The initial item is the full list
            assertEquals(dataset, awaitItem())

            // Arrange: The query is updated
            viewModel.updateQuery("Exercise")

            // Assert: The new, filtered list is ordered by fuzzySearch
            val filteredList = awaitItem()
            assertContentEquals(
                listOf(
                    UiExerciseDC(name = "Exercise", force = Force.PULL),
                    UiExerciseDC(name = "Pull exercise", force = Force.PULL),
                    UiExerciseDC(name = "Push exercise", force = Force.PUSH)
                ),
                filteredList
            )
        }
    }

    @Test
    fun `updateFilter updates the filtered list immediately`() = runTest {
        viewModel.filteredExerciseList.test {
            // Assert: Initial full list
            assertEquals(dataset, awaitItem())

            // Arrange: The filter is updated
            viewModel.updateFilter(FilterValue(force = Force.PULL))

            // Assert: The list is filtered immediately
            val filteredList = awaitItem()
            assertContentEquals(
                listOf(
                    UiExerciseDC(name = "Pull exercise", force = Force.PULL),
                    UiExerciseDC(name = "Exercise", force = Force.PULL)
                ),
                filteredList
            )
        }
    }

    @Test
    fun `list is filtered by both query and filter value`() = runTest(
        mainDispatcherRule.testDispatcher
    ) {
        viewModel.filteredExerciseList.test {
            // Assert: Initial full list
            assertEquals(dataset, awaitItem())

            // Arrange: A filter is applied first
            viewModel.updateFilter(FilterValue(force = Force.PULL))
            assertEquals(2, awaitItem().size) // Pull exercise

            // Arrange: A query is then applied
            viewModel.updateQuery("Exercise")
            mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(301L)

            // Assert: The final list ordered by fuzzySearch
            val finalList = awaitItem()
            assertContentEquals(
                listOf(
                    UiExerciseDC(name = "Exercise", force = Force.PULL),
                    UiExerciseDC(name = "Pull exercise", force = Force.PULL),
                ),
                finalList
            )
        }
    }

    @Test
    fun `when user queries a non present exercise - filtered exercises list is empty`() = runTest {
        viewModel.filteredExerciseList.test {
            // Assert: Initial full list
            assertEquals(dataset, awaitItem())

            // Arrange: A query is then applied
            viewModel.updateQuery("This query should produce an empty list")

            // Assert: List should be empty because fuzzySearch filters all exercises having a
            // name with a match score lower than 60 %
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun `when user apply a filter of non present exercise - filtered exercises list is empty`() =
        runTest {
            viewModel.filteredExerciseList.test {
                // Assert: Initial full list
                assertEquals(dataset, awaitItem())

                // Arrange: A filter is applied first
                viewModel.updateFilter(FilterValue(force = Force.STATIC))

                // Assert: List should be empty because there aren't exercises with such property
                assertTrue(awaitItem().isEmpty())
            }
        }
}
