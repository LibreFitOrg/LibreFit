/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.settings

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.librefit.MainDispatcherRule
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.enums.userPreferences.Language
import org.librefit.enums.userPreferences.ThemeMode

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsScreenViewModelTest {
    // MainDispatcherRule to control coroutine execution
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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

    @Before
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

        coEvery { userPreferencesRepository.saveLanguage(any()) } answers {
            language.value = Language.entries.find { it.code == firstArg() }!!
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

        // Arrange: Create the ViewModel instance with the mock repository
        viewModel = SettingsScreenViewModel(userPreferencesRepository)
    }

    @Test
    fun `initial state - language follows system`() = runTest {
        assertThat(viewModel.language.value).isEqualTo(Language.SYSTEM)
    }

    @Test
    fun `initial state - theme follows system`() = runTest {
        assertThat(viewModel.themeMode.value).isEqualTo(ThemeMode.SYSTEM)
    }

    @Test
    fun `initial state - keep screen is on`() = runTest {
        assertThat(viewModel.keepScreenOn.value).isTrue()
    }

    @Test
    fun `initial state - material mode is off`() = runTest {
        assertThat(viewModel.materialMode.value).isFalse()
    }

    @Test
    fun `initial state - rest timer is is on`() = runTest {
        assertThat(viewModel.restTimerSoundOn.value).isTrue()
    }

    @Test
    fun `initial state - is supporter is is false`() = runTest {
        assertThat(viewModel.isSupporter.value).isFalse()
    }

    @Test
    fun `initial state - dismiss scroll wheel automatically is off`() = runTest {
        assertThat(viewModel.dismissScrollWheelInputAutomatically.value).isFalse()
    }

    @Test
    fun `when updating preferences - preferences must match the update input`() = runTest {
        viewModel.preferences.test {
            // Initial emission
            assertThat(awaitItem()).isNull()

            // Act: update preferences
            val newPreferences = Language.entries
            viewModel.updatePreferences(newPreferences)

            // Assert: preferences has the correct value
            assertThat(awaitItem()).isEqualTo(newPreferences)
        }
    }

    @Test
    fun `when updating preferences - current preference must also update`() = runTest {
        viewModel.currentPreference.test {
            // Initial emission
            assertThat(awaitItem()).isNull()

            // Act: update preferences, it triggers current preference update
            val newPreferences = Language.entries
            viewModel.updatePreferences(newPreferences)

            // Assert: current preference reflects the correct preference and its value
            val value = awaitItem()
            assertThat(value).isInstanceOf(Language::class.java)
            assertThat(value).isEqualTo(Language.SYSTEM)
        }
    }

    @Test
    fun `when a preference updates - current preference must update if preferences is not null`() =
        runTest {
            // Arrange: expected value
            val expected = Language.ENGLISH

            viewModel.currentPreference.test {
                // Initial emission
                assertThat(awaitItem()).isNull()
                viewModel.saveLanguage(expected)
                viewModel.updatePreferences(Language.entries)
                assertThat(awaitItem()).isEqualTo(expected)
            }
        }

    @Test
    fun `when a preference updates - current preference must be null if preferences is null`() =
        runTest {
            viewModel.currentPreference.test {
                // Initial emission
                assertThat(awaitItem()).isNull()
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
            assertThat(awaitItem()).isEqualTo(Language.SYSTEM)
            viewModel.saveLanguage(expected)
            assertThat(awaitItem()).isEqualTo(expected)
        }
    }

    @Test
    fun `theme updates correctly`() = runTest {
        // Arrange: set expected value
        val expected = ThemeMode.LIGHT

        viewModel.themeMode.test {
            // Initial emission
            assertThat(awaitItem()).isEqualTo(ThemeMode.SYSTEM)
            viewModel.saveThemeMode(expected)
            assertThat(awaitItem()).isEqualTo(expected)
        }
    }

    @Test
    fun `material mode updates correctly`() = runTest {
        // Arrange: set expected value
        val expected = true

        viewModel.materialMode.test {
            // Initial emission
            assertThat(awaitItem()).isEqualTo(false)
            viewModel.saveMaterialMode(expected)
            assertThat(awaitItem()).isEqualTo(expected)
        }
    }

    @Test
    fun `keep screen on mode updates correctly`() = runTest {
        // Arrange: set expected value
        val expected = false

        viewModel.keepScreenOn.test {
            // Initial emission
            assertThat(awaitItem()).isEqualTo(true)
            viewModel.saveWorkoutScreenOn(expected)
            assertThat(awaitItem()).isEqualTo(expected)
        }
    }

    @Test
    fun `rest timer sound on mode updates correctly`() = runTest {
        // Arrange: set expected value
        val expected = false

        viewModel.restTimerSoundOn.test {
            // Initial emission
            assertThat(awaitItem()).isEqualTo(true)
            viewModel.saveRestTimerSoundOn(expected)
            assertThat(awaitItem()).isEqualTo(expected)
        }
    }

    @Test
    fun `is workout header sticky updates correctly`() = runTest {
        val expected = false

        viewModel.isWorkoutHeaderSticky.test {
            assertThat(awaitItem()).isEqualTo(true)
            viewModel.saveIsWorkoutHeaderSticky(expected)
            assertThat(awaitItem()).isEqualTo(expected)
        }
    }

    @Test
    fun `use scroll wheel for input updates correctly`() = runTest {
        val expected = false

        viewModel.useScrollWheelForInput.test {
            assertThat(awaitItem()).isEqualTo(true)
            viewModel.saveUseScrollWheelForInput(expected)
            assertThat(awaitItem()).isEqualTo(expected)
        }
    }

    @Test
    fun `dismiss scroll wheel automatically updates correctly`() = runTest {
        val expected = true

        viewModel.dismissScrollWheelInputAutomatically.test {
            assertThat(awaitItem()).isFalse()
            viewModel.saveDismissScrollWheelInputAutomatically(expected)
            assertThat(awaitItem()).isEqualTo(expected)
        }
    }

    @Test
    fun `update dialog preference with language works`() = runTest {
        val expected = Language.ENGLISH

        viewModel.language.test {
            assertThat(awaitItem()).isEqualTo(Language.SYSTEM)
            viewModel.updateDialogPreference(expected)
            assertThat(awaitItem()).isEqualTo(expected)
        }
    }

    @Test
    fun `update dialog preference with theme mode works`() = runTest {
        val expected = ThemeMode.DARK

        viewModel.themeMode.test {
            assertThat(awaitItem()).isEqualTo(ThemeMode.SYSTEM)
            viewModel.updateDialogPreference(expected)
            assertThat(awaitItem()).isEqualTo(expected)
        }
    }
}