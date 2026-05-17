/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.shared

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.librefit.db.entity.ExerciseDC
import org.librefit.db.repository.UserPreferencesRepository

class SharedViewModelTest {
    // The mock repository
    private lateinit var userPreferencesRepository: UserPreferencesRepository

    // The class to test
    private lateinit var viewModel: SharedViewModel

    // Controllable flow to simulate repository emission
    private lateinit var showWelcomeScreen: MutableStateFlow<Boolean>
    private lateinit var requestPermissionNextTime: MutableStateFlow<Boolean>
    private lateinit var isSupporter: MutableStateFlow<Boolean>

    @Before
    fun setUp() {
        // Arrange: Create a mock for the repository
        userPreferencesRepository = mockk()
        showWelcomeScreen = MutableStateFlow(true)
        requestPermissionNextTime = MutableStateFlow(true)
        isSupporter = MutableStateFlow(false)

        // Arrange: Tell the mock what to return when these are accessed
        every { userPreferencesRepository.showWelcomeScreen } returns showWelcomeScreen
        every { userPreferencesRepository.requestPermissionsNextTime } returns requestPermissionNextTime
        every { userPreferencesRepository.isSupporter } returns isSupporter

        coEvery { userPreferencesRepository.saveShowWelcomeScreen(any()) } answers {
            showWelcomeScreen.value = firstArg()
        }
        coEvery { userPreferencesRepository.saveRequestPermissionsNextTime(any()) } answers {
            requestPermissionNextTime.value = firstArg()
        }
        coEvery { userPreferencesRepository.saveIsSupporter(any()) } answers {
            isSupporter.value = firstArg()
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
    fun `initial state - request permission again is true`() = runTest {
        assertThat(viewModel.requestPermissionNextTime.value).isTrue()
    }

    @Test
    fun `initial state - is supporter is false`() = runTest {
        assertThat(viewModel.isSupporter.value).isFalse()
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

    @Test
    fun `request permission again updates correctly`() = runTest {
        viewModel.requestPermissionNextTime.test {
            // Initial emission
            assertThat(awaitItem()).isTrue()

            // Act: update preference
            viewModel.saveRequestPermissionAgainPreference(false)

            // Assert: update is correct
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `is supporter updates correctly`() = runTest {
        viewModel.isSupporter.test {
            // Initial emission
            assertThat(awaitItem()).isFalse()

            // Act: update preference
            viewModel.updateIsSupporter(true)

            // Assert: update is correct
            assertThat(awaitItem()).isTrue()
        }
    }
}