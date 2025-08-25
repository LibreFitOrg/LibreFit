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

package org.librefit.ui.screens.settings

import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
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

    // Captured objects
    private val key = slot<Preferences.Key<Any>>()
    private val valueKey = slot<Any>()

    @Before
    fun setUp() {
        // Arrange: Create a mock for the repository
        userPreferencesRepository = mockk()
        language = MutableStateFlow(Language.SYSTEM)
        themeMode = MutableStateFlow(ThemeMode.SYSTEM)
        keepScreenOn = MutableStateFlow(true)
        materialModeOn = MutableStateFlow(false)

        // Arrange: Tell the mock what to return when these are accessed
        every { userPreferencesRepository.language } returns language
        every { userPreferencesRepository.themeMode } returns themeMode
        every { userPreferencesRepository.workoutScreenOn } returns keepScreenOn
        every { userPreferencesRepository.materialMode } returns materialModeOn
        coEvery {
            userPreferencesRepository.savePreference(
                capture(key),
                capture(valueKey)
            )
        } answers {
            val value = valueKey.captured
            when (key.captured) {
                UserPreferencesRepository.languageKey -> {
                    language.value = Language.entries.find { it.code == value }
                        ?: error("Invalid language value")
                }

                UserPreferencesRepository.themeModeKey -> {
                    themeMode.value =
                        ThemeMode.entries.find { it.value == value } ?: error("Invalid theme value")
                }

                UserPreferencesRepository.keepOnWorkoutScreenKey -> {
                    keepScreenOn.value = value as Boolean
                }

                UserPreferencesRepository.materialModeKey -> {
                    materialModeOn.value = value as Boolean
                }

                else -> error("Invalid key")
            }
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
    fun `initial state - keep screen on is true`() = runTest {
        assertThat(viewModel.keepScreenOn.value).isTrue()
    }

    @Test
    fun `initial state - material mode is off`() = runTest {
        assertThat(viewModel.materialMode.value).isFalse()
    }

    @Test
    fun `initial state - preferences is null`() = runTest {
        assertThat(viewModel.preferences.value).isNull()
    }

    @Test
    fun `initial state - current preference is null`() = runTest {
        assertThat(viewModel.currentPreference.value).isNull()
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

                // Act: update preference
                viewModel.savePreference(UserPreferencesRepository.languageKey, expected.code)


                // Act: update preferences
                val newPreferences = Language.entries
                viewModel.updatePreferences(newPreferences)

                // Assert: current preference match the preferences type
                assertThat(awaitItem()).isEqualTo(expected)
            }
        }

    @Test
    fun `when a preference updates - current preference must be null if preferences is null`() =
        runTest {
            viewModel.currentPreference.test {
                // Initial emission
                assertThat(awaitItem()).isNull()

                // Act: update preference
                viewModel.savePreference(
                    UserPreferencesRepository.languageKey,
                    Language.ENGLISH.code
                )

                // Assert: current preference must be null (because preferences is null), so no more emissions
                expectNoEvents()
            }
        }


    @Test
    fun `when updating preferences with empty list - it must throw an illegal argument exception`() =
        runTest {
            // Arrange: update preferences with a empty list
            val exception = assertThrows(IllegalArgumentException::class.java) {
                viewModel.updatePreferences(emptyList())
            }

            // Assert: the correct exception is thrown
            assertThat(exception).isInstanceOf(IllegalArgumentException::class.java)
            assertThat(exception).hasMessageThat().isEqualTo("Preferences must be not empty")
        }

    @Test
    fun `language updates correctly`() = runTest {
        // Arrange: set expected value
        val expected = Language.ENGLISH

        viewModel.language.test {
            // Initial emission
            assertThat(awaitItem()).isEqualTo(Language.SYSTEM)

            // Act: update preference
            viewModel.savePreference(UserPreferencesRepository.languageKey, expected.code)

            // Assert: update is correct
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

            // Act: update preference
            viewModel.savePreference(UserPreferencesRepository.themeModeKey, expected.value)

            // Assert: update is correct
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

            // Act: update preference
            viewModel.savePreference(UserPreferencesRepository.materialModeKey, expected)

            // Assert: update is correct
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

            // Act: update preference
            viewModel.savePreference(UserPreferencesRepository.keepOnWorkoutScreenKey, expected)

            // Assert: update is correct
            assertThat(awaitItem()).isEqualTo(expected)
        }
    }

}