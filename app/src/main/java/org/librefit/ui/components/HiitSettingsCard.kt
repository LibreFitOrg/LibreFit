/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.librefit.R
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.ui.theme.LibreFitTheme

/**
 * Toggle + duration input that lets the user enable HIIT auto-advance for a single
 * DURATION exercise. Visually subordinate to the exercise card it sits next to.
 *
 * When [autoAdvanceSets] is on, the parent screen renders [HiitCountdownCard] for this
 * exercise. When off, the exercise behaves like a normal duration exercise.
 */
@Composable
fun HiitSettingsCard(
    autoAdvanceSets: Boolean,
    targetDurationSeconds: Int,
    onAutoAdvanceChange: (Boolean) -> Unit,
    onTargetDurationChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.hiit_mode),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.hiit_mode_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = autoAdvanceSets,
                    onCheckedChange = onAutoAdvanceChange
                )
            }
            AnimatedVisibility(visible = autoAdvanceSets) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = if (targetDurationSeconds == 0) "" else targetDurationSeconds.toString(),
                        onValueChange = { input ->
                            // Only accept digits; treat empty as 0. Cap at 9999 to avoid silly values.
                            val sanitized = input.filter { it.isDigit() }.take(4)
                            onTargetDurationChange(sanitized.toIntOrNull() ?: 0)
                        },
                        label = { Text(stringResource(R.string.hiit_target_duration)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun HiitSettingsCardOnPreview() {
    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        HiitSettingsCard(
            autoAdvanceSets = true,
            targetDurationSeconds = 30,
            onAutoAdvanceChange = {},
            onTargetDurationChange = {}
        )
    }
}

@Preview
@Composable
private fun HiitSettingsCardOffPreview() {
    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        HiitSettingsCard(
            autoAdvanceSets = false,
            targetDurationSeconds = 0,
            onAutoAdvanceChange = {},
            onTargetDurationChange = {}
        )
    }
}
