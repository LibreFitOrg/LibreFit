/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.exercises

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.librefit.MainDispatcherRule
import org.librefit.db.repository.DatasetRepository
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.enums.exercise.FilterValue
import org.librefit.enums.exercise.Force
import org.librefit.ui.models.UiExerciseDC

@ExperimentalCoroutinesApi
class ExercisesScreenViewModelTest {

    // MainDispatcherRule to control coroutine execution
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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

    @Before
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
        assertThat(viewModel.query.value).isEmpty()
    }

    @Test
    fun `initial state - debounced query is empty`() = runTest {
        viewModel.debouncedQuery.test {
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun `initial state - filter is empty`() = runTest {
        assertThat(viewModel.filterValue.value).isEqualTo(FilterValue())
    }

    @Test
    fun `initial state - filtered exercise list is equal to dataset`() = runTest {
        assertThat(viewModel.filteredExerciseList.value).containsExactlyElementsIn(dataset)
    }

    @Test
    fun `updateQuery updates the query state flow`() = runTest {
        val query = "query"
        // Arrange: The query is updated
        viewModel.updateQuery(query)

        // Assert: The immediate query state is updated
        assertThat(viewModel.query.value).isEqualTo(query)
    }

    @Test
    fun `filteredExerciseList updates after query debounce period`() = runTest(
        context = mainDispatcherRule.testDispatcher
    ) {
        viewModel.filteredExerciseList.test {
            // Assert: The initial item is the full list
            assertThat(awaitItem()).containsExactlyElementsIn(dataset)

            // Arrange: The query is updated
            viewModel.updateQuery("Exercise")

            // Arrange: Advance the virtual clock past the debounce timeout
            mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(301L)

            // Assert: The new, filtered list is ordered by fuzzySearch
            val filteredList = awaitItem()
            assertThat(filteredList).containsExactly(
                UiExerciseDC(name = "Exercise", force = Force.PULL),
                UiExerciseDC(name = "Pull exercise", force = Force.PULL),
                UiExerciseDC(name = "Push exercise", force = Force.PUSH)
            ).inOrder()
        }
    }

    @Test
    fun `filteredExerciseList updates after last query debounce period`() = runTest(
        context = mainDispatcherRule.testDispatcher
    ) {
        viewModel.filteredExerciseList.test {
            // Assert: The initial item is the full list
            assertThat(awaitItem()).containsExactlyElementsIn(dataset)

            // Arrange: The query is updated multiple times and advance the virtual clock past the debounce timeout
            viewModel.updateQuery("Exe")
            mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(100L)

            viewModel.updateQuery("Exerc")
            mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(100L)

            viewModel.updateQuery("Exercise")
            mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(301L)

            // Assert: The new, filtered list is ordered by fuzzySearch
            val filteredList = awaitItem()
            assertThat(filteredList).containsExactly(
                UiExerciseDC(name = "Exercise", force = Force.PULL),
                UiExerciseDC(name = "Pull exercise", force = Force.PULL),
                UiExerciseDC(name = "Push exercise", force = Force.PUSH)
            ).inOrder()
        }
    }

    @Test
    fun `filteredExerciseList doesn't update before query debounce period`() = runTest(
        context = mainDispatcherRule.testDispatcher
    ) {
        viewModel.filteredExerciseList.test {
            // Assert: The initial item is the full list
            assertThat(awaitItem()).containsExactlyElementsIn(dataset)

            // Arrange: The query is updated
            viewModel.updateQuery("Exercise")

            // Assert: The new, filtered list is ordered by fuzzySearch
            val filteredList = awaitItem()
            assertThat(filteredList).containsExactly(
                UiExerciseDC(name = "Exercise", force = Force.PULL),
                UiExerciseDC(name = "Pull exercise", force = Force.PULL),
                UiExerciseDC(name = "Push exercise", force = Force.PUSH)
            ).inOrder()
        }
    }

    @Test
    fun `updateFilter updates the filtered list immediately`() = runTest {
        viewModel.filteredExerciseList.test {
            // Assert: Initial full list
            assertThat(awaitItem()).containsExactlyElementsIn(dataset)

            // Arrange: The filter is updated
            viewModel.updateFilter(FilterValue(force = Force.PULL))

            // Assert: The list is filtered immediately
            val filteredList = awaitItem()
            assertThat(filteredList).containsExactly(
                UiExerciseDC(name = "Pull exercise", force = Force.PULL),
                UiExerciseDC(name = "Exercise", force = Force.PULL)
            ).inOrder()
        }
    }

    @Test
    fun `list is filtered by both query and filter value`() = runTest(
        mainDispatcherRule.testDispatcher
    ) {
        viewModel.filteredExerciseList.test {
            // Assert: Initial full list
            assertThat(awaitItem()).containsExactlyElementsIn(dataset)

            // Arrange: A filter is applied first
            viewModel.updateFilter(FilterValue(force = Force.PULL))
            assertThat(awaitItem()).hasSize(2) // Pull exercise

            // Arrange: A query is then applied
            viewModel.updateQuery("Exercise")
            mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(301L)

            // Assert: The final list ordered by fuzzySearch
            val finalList = awaitItem()
            assertThat(finalList).containsExactly(
                UiExerciseDC(name = "Exercise", force = Force.PULL),
                UiExerciseDC(name = "Pull exercise", force = Force.PULL),
            ).inOrder()
        }
    }

    @Test
    fun `when user queries a non present exercise - filtered exercises list is empty`() = runTest {
        viewModel.filteredExerciseList.test {
            // Assert: Initial full list
            assertThat(awaitItem()).containsExactlyElementsIn(dataset)

            // Arrange: A query is then applied
            viewModel.updateQuery("This query should produce an empty list")

            // Assert: List should be empty because fuzzySearch filters all exercises having a
            // name with a match score lower than 60 %
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun `when user apply a filter of non present exercise - filtered exercises list is empty`() =
        runTest {
            viewModel.filteredExerciseList.test {
                // Assert: Initial full list
                assertThat(awaitItem()).containsExactlyElementsIn(dataset)

                // Arrange: A filter is applied first
                viewModel.updateFilter(FilterValue(force = Force.STATIC))

                // Assert: List should be empty because there aren't exercises with such property
                assertThat(awaitItem()).isEmpty()
            }
        }
}