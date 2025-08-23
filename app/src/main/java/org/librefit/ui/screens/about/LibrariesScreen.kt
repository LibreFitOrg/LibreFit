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

package org.librefit.ui.screens.about

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import com.mikepenz.aboutlibraries.ui.compose.android.rememberLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import org.librefit.R
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.dialogs.UrlActionDialog
import org.librefit.ui.theme.LibreFitTheme

@Composable
fun LibrariesScreen(navigateBack: () -> Unit) {
    val url = remember { mutableStateOf<String?>(null) }

    url.value?.let {
        UrlActionDialog(it) { url.value = null }
    }

    val libs = rememberLibraries()

    LibreFitScaffold(
        title = AnnotatedString(stringResource(R.string.libraries)),
        navigateBack = navigateBack
    ) { innerPadding ->
        LibrariesContainer(
            contentPadding = innerPadding,
            libraries = libs.value,
            showDescription = true,
            onLibraryClick = {
                url.value = it.website ?: ""
            }
        )
    }
}

@Preview
@Composable
private fun LibrariesScreenPreview() {
    LibreFitTheme(false, true) {
        LibrariesScreen { }
    }
}