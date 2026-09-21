/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.settings

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.librefit.MainDispatcherRule
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.enums.userPreferences.Language
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.enums.userPreferences.UnitSystem
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsScreenViewModelTest {
    // MainDispatcherRule to control coroutine execution
    private val mainDispatcherRule = MainDispatcherRule()

    // The mock repository
    private lateinit var userPreferencesRepository: UserPreferencesRepository

    // The class under test
    private lateinit var viewModel: SettingsScreenViewModel

    // Controllable flows to simulate repository emissions
    private lateinit var language: MutableStateFlow<Language>
    private lateinit var themeMode: MutableStateFlow<ThemeMode>
    private lateinit var keepScreenOn: MutableStateFlow<Boolean>
    private lateinit var materialModeOn: MutableStateFlow<Boolean>
    private lateinit var restTimerSoundOn: MutableStateFlow<Boolean>
    private lateinit var isSupporter: MutableStateFlow<Boolean>
    private lateinit var isWorkoutHeaderSticky: MutableStateFlow<Boolean>
    private lateinit var useScrollWheelForInput: MutableStateFlow<Boolean>
    private lateinit var dismissScrollWheelAutomatically: MutableStateFlow<Boolean>
    private lateinit var showExercisesImages: MutableStateFlow<Boolean?>
    private lateinit var unitSystem: MutableStateFlow<UnitSystem>

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
        userPreferencesRepository = mockk()
        language = MutableStateFlow(Language.SYSTEM)
        themeMode = MutableStateFlow(ThemeMode.SYSTEM)
        keepScreenOn = MutableStateFlow(true)
        materialModeOn = MutableStateFlow(false)
        restTimerSoundOn = MutableStateFlow(true)
        isSupporter = MutableStateFlow(false)
        isWorkoutHeaderSticky = MutableStateFlow(true)
        useScrollWheelForInput = MutableStateFlow(true)
        dismissScrollWheelAutomatically = MutableStateFlow(false)
        showExercisesImages = MutableStateFlow(null)
        unitSystem = MutableStateFlow(UnitSystem.METRIC)

        // Arrange: Tell the mock what to return when these are accessed
        every { userPreferencesRepository.language } returns language
        every { userPreferencesRepository.themeMode } returns themeMode
        every { userPreferencesRepository.workoutScreenOn } returns keepScreenOn
        every { userPreferencesRepository.materialMode } returns materialModeOn
        every { userPreferencesRepository.restTimerSoundOn } returns restTimerSoundOn
        every { userPreferencesRepository.isSupporter } returns isSupporter
        every { userPreferencesRepository.isWorkoutHeaderSticky } returns isWorkoutHeaderSticky
        every { userPreferencesRepository.useScrollWheelForInput } returns useScrollWheelForInput
        every { userPreferencesRepository.dismissScrollWheelInputAutomatically } returns dismissScrollWheelAutomatically
        every { userPreferencesRepository.showExercisesImages } returns showExercisesImages
        every { userPreferencesRepository.unitSystem } returns unitSystem

        every { userPreferencesRepository.saveLanguage(any()) } answers {
            language.value = firstArg()
        }
        coEvery { userPreferencesRepository.saveThemeMode(any()) } answers {
            themeMode.value = firstArg()
        }
        coEvery { userPreferencesRepository.saveMaterialMode(any()) } answers {
            materialModeOn.value = firstArg()
        }
        coEvery { userPreferencesRepository.saveWorkoutScreenOn(any()) } answers {
            keepScreenOn.value = firstArg()
        }
        coEvery { userPreferencesRepository.saveRestTimerSoundOn(any()) } answers {
            restTimerSoundOn.value = firstArg()
        }
        coEvery { userPreferencesRepository.saveIsSupporter(any()) } answers {
            isSupporter.value = firstArg()
        }
        coEvery { userPreferencesRepository.saveIsWorkoutHeaderSticky(any()) } answers {
            isWorkoutHeaderSticky.value = firstArg()
        }
        coEvery { userPreferencesRepository.saveUseScrollWheelForInput(any()) } answers {
            useScrollWheelForInput.value = firstArg()
        }
        coEvery { userPreferencesRepository.saveDismissScrollWheelInputAutomatically(any()) } answers {
            dismissScrollWheelAutomatically.value = firstArg()
        }
        coEvery { userPreferencesRepository.saveShowExercisesImages(any()) } answers {
            showExercisesImages.value = firstArg()
        }

        // Arrange: Create the ViewModel instance with the mock repository
        viewModel = SettingsScreenViewModel(userPreferencesRepository)
    }

    @Test
    fun `initial state - language follows system`() = runTest {
        assertEquals(Language.SYSTEM, viewModel.language.value)
    }

    @Test
    fun `initial state - theme follows system`() = runTest {
        assertEquals(ThemeMode.SYSTEM, viewModel.themeMode.value)
    }

    @Test
    fun `initial state - keep screen is on`() = runTest {
        assertTrue(viewModel.keepScreenOn.value)
    }

    @Test
    fun `initial state - material mode is off`() = runTest {
        assertFalse(viewModel.materialMode.value)
    }

    @Test
    fun `initial state - rest timer is is on`() = runTest {
        assertTrue(viewModel.restTimerSoundOn.value)
    }

    @Test
    fun `initial state - is supporter is is false`() = runTest {
        assertFalse(viewModel.isSupporter.value)
    }

    @Test
    fun `initial state - dismiss scroll wheel automatically is off`() = runTest {
        assertFalse(viewModel.dismissScrollWheelInputAutomatically.value)
    }

    @Test
    fun `initial state - show images is null`() = runTest {
        assertNull(viewModel.showExercisesImages.value)
    }

    @Test
    fun `show exercises images updates correctly`() = runTest {
        val expected = true

        viewModel.showExercisesImages.test {
            assertNull(awaitItem())
            viewModel.saveShowExercisesImages(expected)
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `when updating preferences - preferences must match the update input`() = runTest {
        viewModel.preferences.test {
            // Initial emission
            assertNull(awaitItem())

            // Act: update preferences
            val newPreferences = Language.entries
            viewModel.updatePreferences(newPreferences)

            // Assert: preferences has the correct value
            assertEquals(newPreferences, awaitItem())
        }
    }

    @Test
    fun `when updating preferences - current preference must also update`() = runTest {
        viewModel.currentPreference.test {
            // Initial emission
            assertNull(awaitItem())

            // Act: update preferences, it triggers current preference update
            val newPreferences = Language.entries
            viewModel.updatePreferences(newPreferences)

            // Assert: current preference reflects the correct preference and its value
            val value = awaitItem()
            assertIs<Language>(value)
            assertEquals(Language.SYSTEM, value)
        }
    }

    @Test
    fun `when a preference updates - current preference must update if preferences is not null`() =
        runTest {
            // Arrange: expected value
            val expected = Language.ENGLISH

            viewModel.currentPreference.test {
                // Initial emission
                assertNull(awaitItem())
                viewModel.saveLanguage(expected)
                viewModel.updatePreferences(Language.entries)
                assertEquals(expected, awaitItem())
            }
        }

    @Test
    fun `when a preference updates - current preference must be null if preferences is null`() =
        runTest {
            viewModel.currentPreference.test {
                // Initial emission
                assertNull(awaitItem())
                viewModel.saveLanguage(Language.ENGLISH)
                expectNoEvents()
            }
        }

    @Test
    fun `language updates correctly`() = runTest {
        // Arrange: set expected value
        val expected = Language.ENGLISH

        viewModel.language.test {
            // Initial emission
            assertEquals(Language.SYSTEM, awaitItem())
            viewModel.saveLanguage(expected)
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `theme updates correctly`() = runTest {
        // Arrange: set expected value
        val expected = ThemeMode.LIGHT

        viewModel.themeMode.test {
            // Initial emission
            assertEquals(ThemeMode.SYSTEM, awaitItem())
            viewModel.saveThemeMode(expected)
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `material mode updates correctly`() = runTest {
        // Arrange: set expected value
        val expected = true

        viewModel.materialMode.test {
            // Initial emission
            assertEquals(false, awaitItem())
            viewModel.saveMaterialMode(expected)
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `keep screen on mode updates correctly`() = runTest {
        // Arrange: set expected value
        val expected = false

        viewModel.keepScreenOn.test {
            // Initial emission
            assertEquals(true, awaitItem())
            viewModel.saveWorkoutScreenOn(expected)
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `rest timer sound on mode updates correctly`() = runTest {
        // Arrange: set expected value
        val expected = false

        viewModel.restTimerSoundOn.test {
            // Initial emission
            assertEquals(true, awaitItem())
            viewModel.saveRestTimerSoundOn(expected)
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `is workout header sticky updates correctly`() = runTest {
        val expected = false

        viewModel.isWorkoutHeaderSticky.test {
            assertEquals(true, awaitItem())
            viewModel.saveIsWorkoutHeaderSticky(expected)
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `use scroll wheel for input updates correctly`() = runTest {
        val expected = false

        viewModel.useScrollWheelForInput.test {
            assertEquals(true, awaitItem())
            viewModel.saveUseScrollWheelForInput(expected)
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `dismiss scroll wheel automatically updates correctly`() = runTest {
        val expected = true

        viewModel.dismissScrollWheelInputAutomatically.test {
            assertFalse(awaitItem())
            viewModel.saveDismissScrollWheelInputAutomatically(expected)
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `update dialog preference with language works`() = runTest {
        val expected = Language.ENGLISH

        viewModel.language.test {
            assertEquals(Language.SYSTEM, awaitItem())
            viewModel.updateDialogPreference(expected)
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `update dialog preference with theme mode works`() = runTest {
        val expected = ThemeMode.DARK

        viewModel.themeMode.test {
            assertEquals(ThemeMode.SYSTEM, awaitItem())
            viewModel.updateDialogPreference(expected)
            assertEquals(expected, awaitItem())
        }
    }
}
