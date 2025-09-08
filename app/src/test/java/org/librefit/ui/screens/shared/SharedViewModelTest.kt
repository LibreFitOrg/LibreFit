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

package org.librefit.ui.screens.shared

import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.librefit.db.entity.ExerciseDC
import org.librefit.db.repository.UserPreferencesRepository

class SharedViewModelTest {
    // The mock repository
    private lateinit var userPreferencesRepository: UserPreferencesRepository

    // Captured objects
    private val key = slot<Preferences.Key<Any>>()
    private val valueKey = slot<Any>()

    // The class to test
    private lateinit var viewModel: SharedViewModel

    // Controllable flow to simulate repository emission
    private lateinit var showWelcomeScreen: MutableStateFlow<Boolean>

    @Before
    fun setUp() {
        // Arrange: Create a mock for the repository
        userPreferencesRepository = mockk()
        showWelcomeScreen = MutableStateFlow(true)

        // Arrange: Tell the mock what to return when these are accessed
        every { userPreferencesRepository.showWelcomeScreen } returns showWelcomeScreen
        coEvery {
            userPreferencesRepository.savePreference(
                capture(key),
                capture(valueKey)
            )
        } answers {
            val value = valueKey.captured
            when (key.captured) {
                UserPreferencesRepository.showWelcomeScreenKey -> {
                    showWelcomeScreen.value = value as Boolean
                }

                else -> error("Invalid key")
            }
        }

        // Arrange: Create the ViewModel instance with the mock repository
        viewModel = SharedViewModel(userPreferencesRepository)
    }

    @Test
    fun `initial state is an empty list`() {
        // Act
        val result = viewModel.getSelectedExercisesList()

        // Assert
        assertThat(result).isEmpty()
    }

    @Test
    fun `initial state - show welcome screen is true`() = runTest {
        assertThat(viewModel.showWelcomeScreen.value).isTrue()
    }

    @Test
    fun getSelectedExercisesList() {
        // Arrange
        val exercises = (0..4).map { ExerciseDC(id = "$it") }

        viewModel.setSelectedExercisesList(exercises)

        // Act (First call)
        val firstResult = viewModel.getSelectedExercisesList()

        // Assert (First call)
        assertThat(firstResult).isEqualTo(exercises)

        // Act (Second call)
        val secondResult = viewModel.getSelectedExercisesList()

        // Assert (Second call)
        assertThat(secondResult).isEmpty()
    }

    @Test
    fun `show welcome screen updates correctly`() = runTest {
        viewModel.showWelcomeScreen.test {
            // Initial emission
            assertThat(awaitItem()).isTrue()

            // Act: update preference
            viewModel.doNotShowWelcomeScreenAgain()

            // Assert: update is correct
            assertThat(awaitItem()).isFalse()
        }
    }

}