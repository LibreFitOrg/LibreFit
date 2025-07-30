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

package org.librefit.ui.screens.exercises

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.librefit.MainDispatcherRule
import org.librefit.data.ExerciseDC
import org.librefit.enums.exercise.FilterValue
import org.librefit.enums.exercise.Force

@ExperimentalCoroutinesApi
class ExercisesScreenViewModelTest {

    // MainCoroutineRule to control coroutine execution
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Test data
    private val testExercises = listOf(
        ExerciseDC(name = "Pull exercise", force = Force.PULL),
        ExerciseDC(name = "Push exercise", force = Force.PUSH),
        ExerciseDC(name = "Exercise", force = Force.PULL)
    )

    private lateinit var viewModel: ExercisesScreenViewModel

    // Setup the ViewModel before each test
    @Before
    fun setUp() {
        // Instantiate the ViewModel directly, passing in test data
        viewModel = ExercisesScreenViewModel(exercisesList = testExercises)
    }

    @Test
    fun `initial state is correct`() = runTest {
        // Then: Assert the initial values are as expected
        assertThat(viewModel.query.value).isEmpty()
        assertThat(viewModel.debouncedQuery.value).isEmpty()
        assertThat(viewModel.filterValue.value).isEqualTo(FilterValue())
        assertThat(viewModel.filteredExerciseList.value).containsExactlyElementsIn(testExercises)
    }

    @Test
    fun `updateQuery updates the query state flow`() = runTest {
        val query = "query"
        // When: The query is updated
        viewModel.updateQuery(query)

        // Then: The immediate query state is updated
        assertThat(viewModel.query.value).isEqualTo(query)
    }

    @Test
    fun `filteredExerciseList updates after query debounce period`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Given a collector on the filtered list
            viewModel.filteredExerciseList.test {
                // Then: The initial item is the full list
                assertThat(awaitItem()).containsExactlyElementsIn(testExercises)

                // When: The query is updated
                viewModel.updateQuery("Exercise")

                // When: Advance the virtual clock past the debounce timeout
                mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(301L)

                // Then: The new, filtered list is ordered by fuzzySearch
                val filteredList = awaitItem()
                assertThat(filteredList).containsExactly(
                    ExerciseDC(name = "Exercise", force = Force.PULL),
                    ExerciseDC(name = "Pull exercise", force = Force.PULL),
                    ExerciseDC(name = "Push exercise", force = Force.PUSH)
                ).inOrder()
            }
        }

    @Test
    fun `updateFilter updates the filtered list immediately`() = runTest {
        viewModel.filteredExerciseList.test {
            // Then: Initial full list
            assertThat(awaitItem()).containsExactlyElementsIn(testExercises)

            // When: The filter is updated
            viewModel.updateFilter(FilterValue(force = Force.PULL))

            // Then: The list is filtered immediately
            val filteredList = awaitItem()
            assertThat(filteredList).containsExactly(
                ExerciseDC(name = "Pull exercise", force = Force.PULL),
                ExerciseDC(name = "Exercise", force = Force.PULL)
            ).inOrder()
        }
    }

    @Test
    fun `list is filtered by both query and filter value`() =
        runTest(mainDispatcherRule.testDispatcher) {
            viewModel.filteredExerciseList.test {
                // Then: Initial full list
                assertThat(awaitItem()).containsExactlyElementsIn(testExercises)

                // When: A filter is applied first
                viewModel.updateFilter(FilterValue(force = Force.PULL))
                assertThat(awaitItem()).hasSize(2) // Pull exercise

                // And When: A query is then applied
                viewModel.updateQuery("Exercise")
                mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(301L)

                // Then: The final list ordered by fuzzySearch
                val finalList = awaitItem()
                assertThat(finalList).containsExactly(
                    ExerciseDC(name = "Exercise", force = Force.PULL),
                    ExerciseDC(name = "Pull exercise", force = Force.PULL),
                ).inOrder()
            }
        }
}