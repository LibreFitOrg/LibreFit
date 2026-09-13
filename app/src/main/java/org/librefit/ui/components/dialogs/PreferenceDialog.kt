/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.components.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.librefit.R
import org.librefit.enums.userPreferences.DialogPreference
import org.librefit.enums.userPreferences.Language
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.enums.userPreferences.UnitSystem
import org.librefit.util.Formatter
import java.text.Collator
import kotlin.time.Duration.Companion.milliseconds

/**
 * Radio-button dialog used to pick a value among the members of a [DialogPreference] family.
 *
 * Entries are listed alphabetically according to their localized label, with the
 * "follow the system" default (see [isSystemDefault]) always pinned on top.
 *
 * @param currentPreference currently selected preference, or `null` when none is selected.
 * @param preferences selectable preferences, e.g. [Language.entries].
 * @param updatePreference invoked with the preference tapped by the user.
 * @param onDismiss invoked when the dialog is dismissed or a preference is tapped.
 */
@Composable
fun PreferenceDialog(
    currentPreference: DialogPreference?,
    preferences: List<DialogPreference>,
    updatePreference: (DialogPreference) -> Unit,
    onDismiss: () -> Unit,
) {
    require(preferences.isNotEmpty()) { "Preferences must be not empty" }

    AlertDialog(
        title = {
            Text(
                text = stringResource(
                    when (preferences.first()) {
                        is Language -> R.string.language
                        is ThemeMode -> R.string.theme
                        is UnitSystem -> R.string.unit_system
                    }
                )
            )
        },
        icon = {
            Icon(
                painter = painterResource(
                    when (preferences.first()) {
                        is Language -> R.drawable.ic_translate
                        is ThemeMode -> R.drawable.ic_dark_mode
                        is UnitSystem -> R.drawable.ic_weight
                    }
                ),
                contentDescription = null
            )
        },
        onDismissRequest = onDismiss,
        confirmButton = { /*The user doesn't need to confirm*/ },
        text = {

            val orderedPreferences = displayOrder(preferences)

            val lazyListState = rememberLazyListState()

            LaunchedEffect(Unit) {
                // Wait before scrolling so user can see it
                delay(500.milliseconds)

                lazyListState.animateScrollToItem(
                    index = orderedPreferences.indexOf(currentPreference).takeUnless { it == -1 }
                        ?: 0
                )
            }

            LazyColumn(
                state = lazyListState,
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                items(orderedPreferences, key = { it }) { preference ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.large)
                            .padding(5.dp)
                            .clickable {
                                onDismiss()
                                updatePreference(preference)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = preference == currentPreference,
                            onClick = {
                                onDismiss()
                                updatePreference(preference)
                            }
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(Formatter.preferenceToStringId(preference)))
                    }
                }
            }
        }
    )
}

/**
 * Orders [preferences] for display: entries flagged as [isSystemDefault] keep
 * their original order on top, the remaining ones are sorted alphabetically by their localized
 * label using a locale-aware [Collator], so that diacritics sort correctly.
 */
@Composable
private fun displayOrder(preferences: List<DialogPreference>): List<DialogPreference> {
    val locale = LocalConfiguration.current.locales[0]
    val collator = remember(locale) { Collator.getInstance(locale) }
    val (systemDefaults, others) = preferences
        .map { it to stringResource(Formatter.preferenceToStringId(it)) }
        .partition { (preference, _) -> preference.isSystemDefault }
    return systemDefaults.map { it.first } +
            others.sortedWith(compareBy(collator) { (_, label) -> label }).map { it.first }
}

/**
 * Whether this preference is the "follow the system" default of its family
 * ([Language.SYSTEM], [ThemeMode.SYSTEM]).
 */
private val DialogPreference.isSystemDefault: Boolean
    get() = when (this) {
        is Language -> this == Language.SYSTEM
        is ThemeMode -> this == ThemeMode.SYSTEM
        is UnitSystem -> false
    }
