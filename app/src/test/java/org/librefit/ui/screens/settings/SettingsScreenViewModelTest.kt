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
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.librefit.MainDispatcherRule
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.enums.healthConnect.HealthConnectSyncOption
import org.librefit.enums.userPreferences.Language
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.health.HealthConnectRepository
import org.librefit.health.HealthConnectSyncManager

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsScreenViewModelTest {
    // MainDispatcherRule to control coroutine execution
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // The mock repository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var healthConnectRepository: HealthConnectRepository
    private lateinit var healthConnectSyncManager: HealthConnectSyncManager

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
    private lateinit var healthConnectEnabled: MutableStateFlow<Boolean>
    private lateinit var healthConnectOptions: Map<HealthConnectSyncOption, MutableStateFlow<Boolean>>

    @Before
    fun setUp() {
        // Arrange: Create a mock for the repository
        userPreferencesRepository = mockk()
        healthConnectRepository = mockk()
        healthConnectSyncManager = mockk()
        language = MutableStateFlow(Language.SYSTEM)
        themeMode = MutableStateFlow(ThemeMode.SYSTEM)
        keepScreenOn = MutableStateFlow(true)
        materialModeOn = MutableStateFlow(false)
        restTimerSoundOn = MutableStateFlow(true)
        isSupporter = MutableStateFlow(false)
        isWorkoutHeaderSticky = MutableStateFlow(true)
        useScrollWheelForInput = MutableStateFlow(true)
        dismissScrollWheelAutomatically = MutableStateFlow(false)
        healthConnectEnabled = MutableStateFlow(false)
        healthConnectOptions = HealthConnectSyncOption.entries.associateWith {
            MutableStateFlow(false)
        }

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
        every { userPreferencesRepository.healthConnectEnabled } returns healthConnectEnabled
        every { userPreferencesRepository.healthConnectSyncEnabled(any()) } answers {
            healthConnectOptions.getValue(
                HealthConnectSyncOption.entries.first { it.preferenceId == firstArg() }
            )
        }
        coEvery { userPreferencesRepository.isHealthConnectSyncEnabled(any()) } answers {
            healthConnectOptions.getValue(
                HealthConnectSyncOption.entries.first { it.preferenceId == firstArg() }
            ).value
        }
        every { healthConnectRepository.allPermissions } returns emptySet()
        every { healthConnectRepository.permissionsFor(any()) } returns emptySet()
        every { healthConnectRepository.isAvailable() } returns false
        coEvery { healthConnectRepository.grantedPermissions() } returns emptySet()
        every { healthConnectSyncManager.invalidatePendingSyncs() } returns Unit
        coEvery { healthConnectSyncManager.syncEnabledData(any()) } returns 0

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
        coEvery { userPreferencesRepository.saveHealthConnectEnabled(any()) } answers {
            healthConnectEnabled.value = firstArg()
        }
        coEvery { userPreferencesRepository.saveHealthConnectSyncEnabled(any(), any()) } answers {
            val option = HealthConnectSyncOption.entries.first { it.preferenceId == firstArg() }
            healthConnectOptions.getValue(option).value = secondArg()
        }

        // Arrange: Create the ViewModel instance with the mock repository
        viewModel = SettingsScreenViewModel(
            userPreferencesRepository,
            healthConnectRepository,
            healthConnectSyncManager
        )
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
    fun `health connect switch can be disabled locally`() = runTest {
        healthConnectEnabled.value = true

        viewModel.updateHealthConnectEnabled(false)

        viewModel.healthConnectState.test {
            val state = awaitItem()
            assertThat(state.isEnabled).isFalse()
        }
    }

    @Test
    fun `health connect reports unavailable`() = runTest {
        advanceUntilIdle()

        assertThat(viewModel.healthConnectState.value.isAvailable).isFalse()
    }

    @Test
    fun `health connect refreshes granted permissions`() = runTest {
        val permission = "android.permission.health.READ_WEIGHT"
        every { healthConnectRepository.isAvailable() } returns true
        coEvery { healthConnectRepository.grantedPermissions() } returns setOf(permission)

        viewModel.healthConnectState.test {
            viewModel.refreshHealthConnectState()
            advanceUntilIdle()

            var state = awaitItem()
            while (!state.isAvailable) {
                state = awaitItem()
            }
            assertThat(state.grantedPermissions).containsExactly(permission)
        }
    }

    @Test
    fun `granting a health connect permission enables only its option`() = runTest {
        val option = HealthConnectSyncOption.WEIGHT_READ
        val permission = "android.permission.health.READ_WEIGHT"
        every { healthConnectRepository.isAvailable() } returns true
        every { healthConnectRepository.permissionsFor(option) } returns setOf(permission)
        coEvery { healthConnectRepository.grantedPermissions() } returns setOf(permission)

        viewModel.healthConnectState.test {
            viewModel.updateHealthConnectPermissions(option, setOf(permission))
            advanceUntilIdle()

            var state = awaitItem()
            while (option !in state.enabledOptions) {
                state = awaitItem()
            }
            assertThat(state.isEnabled).isTrue()
            assertThat(state.enabledOptions).containsExactly(option)
        }
    }

    @Test
    fun `initial permission request enables health connect without enabling data types`() = runTest {
        val permission = "android.permission.health.READ_WEIGHT"
        every { healthConnectRepository.allPermissions } returns setOf(permission)
        viewModel = SettingsScreenViewModel(
            userPreferencesRepository,
            healthConnectRepository,
            healthConnectSyncManager
        )

        viewModel.updateHealthConnectPermissions(null, setOf(permission))
        advanceUntilIdle()

        assertThat(healthConnectEnabled.value).isTrue()
        assertThat(healthConnectOptions.values.any { it.value }).isFalse()
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
