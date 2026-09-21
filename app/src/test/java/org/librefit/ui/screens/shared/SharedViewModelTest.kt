/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.shared

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.librefit.db.entity.ExerciseDC
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.enums.userPreferences.Language
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.enums.userPreferences.UnitSystem
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SharedViewModelTest {
    // The mock repository
    private lateinit var userPreferencesRepository: UserPreferencesRepository

    // The class to test
    private lateinit var viewModel: SharedViewModel

    // Controllable flow to simulate repository emission
    private lateinit var showWelcomeScreen: MutableStateFlow<Boolean>
    private lateinit var requestPermissionNextTime: MutableStateFlow<Boolean>
    private lateinit var isSupporter: MutableStateFlow<Boolean>
    private lateinit var unitSystem: MutableStateFlow<UnitSystem>
    private lateinit var themeMode: MutableStateFlow<ThemeMode>
    private lateinit var language: MutableStateFlow<Language>

    @BeforeTest
    fun setUp() {
        // Arrange: Create a mock for the repository
        userPreferencesRepository = mockk()
        showWelcomeScreen = MutableStateFlow(true)
        requestPermissionNextTime = MutableStateFlow(true)
        isSupporter = MutableStateFlow(false)
        unitSystem = MutableStateFlow(UnitSystem.METRIC)
        themeMode = MutableStateFlow(ThemeMode.SYSTEM)
        language = MutableStateFlow(Language.SYSTEM)

        // Arrange: Tell the mock what to return when these are accessed
        every { userPreferencesRepository.showWelcomeScreen } returns showWelcomeScreen
        every { userPreferencesRepository.requestPermissionsNextTime } returns requestPermissionNextTime
        every { userPreferencesRepository.isSupporter } returns isSupporter
        every { userPreferencesRepository.unitSystem } returns unitSystem
        every { userPreferencesRepository.themeMode } returns themeMode
        every { userPreferencesRepository.language } returns language

        coEvery { userPreferencesRepository.saveShowWelcomeScreen(any()) } answers {
            showWelcomeScreen.value = firstArg()
        }
        coEvery { userPreferencesRepository.saveRequestPermissionsNextTime(any()) } answers {
            requestPermissionNextTime.value = firstArg()
        }
        coEvery { userPreferencesRepository.saveIsSupporter(any()) } answers {
            isSupporter.value = firstArg()
        }
        coEvery { userPreferencesRepository.saveUnitSystem(any()) } answers {
            unitSystem.value = firstArg()
        }
        coEvery { userPreferencesRepository.saveThemeMode(any()) } answers {
            themeMode.value = firstArg()
        }
        every { userPreferencesRepository.saveLanguage(any()) } answers {
            language.value = firstArg()
        }

        // Arrange: Create the ViewModel instance with the mock repository
        viewModel = SharedViewModel(userPreferencesRepository)
    }

    @Test
    fun `initial state is an empty list`() {
        // Act
        val result = viewModel.getSelectedExercisesList()

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `initial state - show welcome screen is true`() = runTest {
        assertTrue(viewModel.showWelcomeScreen.value)
    }

    @Test
    fun `initial state - request permission again is true`() = runTest {
        assertTrue(viewModel.requestPermissionNextTime.value)
    }

    @Test
    fun `initial state - is supporter is false`() = runTest {
        assertFalse(viewModel.isSupporter.value)
    }

    @Test
    fun getSelectedExercisesList() {
        // Arrange
        val exercises = (0..4).map { ExerciseDC(id = "$it") }

        viewModel.setSelectedExercisesList(exercises)

        // Act (First call)
        val firstResult = viewModel.getSelectedExercisesList()

        // Assert (First call)
        assertEquals(exercises, firstResult)

        // Act (Second call)
        val secondResult = viewModel.getSelectedExercisesList()

        // Assert (Second call)
        assertTrue(secondResult.isEmpty())
    }

    @Test
    fun `show welcome screen updates correctly`() = runTest {
        viewModel.showWelcomeScreen.test {
            // Initial emission
            assertTrue(awaitItem())

            // Act: update preference
            viewModel.doNotShowWelcomeScreenAgain()

            // Assert: update is correct
            assertFalse(awaitItem())
        }
    }

    @Test
    fun `request permission again updates correctly`() = runTest {
        viewModel.requestPermissionNextTime.test {
            // Initial emission
            assertTrue(awaitItem())

            // Act: update preference
            viewModel.saveRequestPermissionAgainPreference(false)

            // Assert: update is correct
            assertFalse(awaitItem())
        }
    }

    @Test
    fun `initial state - unit system is metric`() = runTest {
        assertEquals(UnitSystem.METRIC, viewModel.unitSystem.value)
    }

    @Test
    fun `initial state - theme mode is system`() = runTest {
        assertEquals(ThemeMode.SYSTEM, viewModel.themeMode.value)
    }

    @Test
    fun `unit system updates correctly`() = runTest {
        viewModel.unitSystem.test {
            // Initial emission
            assertEquals(UnitSystem.METRIC, awaitItem())

            // Act: update preference
            viewModel.saveUnitSystem(UnitSystem.IMPERIAL)

            // Assert: update is correct
            assertEquals(UnitSystem.IMPERIAL, awaitItem())
        }
    }

    @Test
    fun `theme mode updates correctly`() = runTest {
        viewModel.themeMode.test {
            // Initial emission
            assertEquals(ThemeMode.SYSTEM, awaitItem())

            // Act: update preference
            viewModel.saveThemeMode(ThemeMode.DARK)

            // Assert: update is correct
            assertEquals(ThemeMode.DARK, awaitItem())
        }
    }

    @Test
    fun `is supporter updates correctly`() = runTest {
        viewModel.isSupporter.test {
            // Initial emission
            assertFalse(awaitItem())

            // Act: update preference
            viewModel.updateIsSupporter(true)

            // Assert: update is correct
            assertTrue(awaitItem())
        }
    }
}
