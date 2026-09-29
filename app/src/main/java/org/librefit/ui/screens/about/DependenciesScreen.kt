/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.about

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.variant.LibrariesDensity
import com.mikepenz.aboutlibraries.ui.compose.variant.LibrariesVariant
import com.mikepenz.aboutlibraries.ui.compose.variant.LibraryActionKind
import org.librefit.R
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.dialogs.UrlActionDialog
import org.librefit.ui.theme.LibreFitTheme

@Composable
fun DependenciesScreen(navigateBack: () -> Unit) {
    var url by remember { mutableStateOf<String?>(null) }

    url?.let {
        UrlActionDialog(it) { url = null }
    }

    val libs by produceLibraries(R.raw.aboutlibraries)

    LibreFitScaffold(
        title = AnnotatedString(stringResource(R.string.dependencies)),
        navigateBack = navigateBack
    ) { innerPadding ->
        LibrariesContainer(
            libraries = libs,
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding,
            variant = LibrariesVariant.Refined,
            density = LibrariesDensity.Cozy,
            onActionClick = { library, actionKind ->
                val urlToOpen = when (actionKind) {
                    LibraryActionKind.Website -> library.website
                    LibraryActionKind.License -> library.licenses.firstOrNull()?.url
                    LibraryActionKind.Sponsor -> library.funding.firstOrNull()?.url
                    LibraryActionKind.Source -> library.scm?.url
                }

                if (urlToOpen.isNullOrBlank()) {
                    false
                } else {
                    url = urlToOpen
                    true
                }
            }
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