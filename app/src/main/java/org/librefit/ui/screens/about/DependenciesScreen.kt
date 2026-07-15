/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.about

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.style.m3LibrariesStyle
import com.mikepenz.aboutlibraries.ui.compose.variant.Libraries
import org.librefit.R
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.dialogs.UrlActionDialog
import org.librefit.ui.theme.LibreFitTheme

@Composable
fun DependenciesScreen(navigateBack: () -> Unit) {
    val url = remember { mutableStateOf<String?>(null) }

    url.value?.let {
        UrlActionDialog(it) { url.value = null }
    }

    val libs = produceLibraries(R.raw.aboutlibraries)

    LibreFitScaffold(
        title = AnnotatedString(stringResource(R.string.dependencies)),
        navigateBack = navigateBack
    ) { innerPadding ->
        Libraries(
            contentPadding = innerPadding,
            libraries = libs.value?.libraries ?: emptyList(),
            onLibraryClick = {
                url.value = it.website
                url.value == it.website
            },
            style = LibraryDefaults.m3LibrariesStyle()
        )
    }
}

@Preview
@Composable
private fun LibrariesScreenPreview() {
    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        DependenciesScreen { }
    }
}