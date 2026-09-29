/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.components

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import org.librefit.R

/**
 * It returns the app name with material theme style and with the word "Libre" colored with the
 * primary color.
 *
 * The app name is assembled from two independent strings ([R.string.app_name_first_part] and
 * [R.string.app_name_second_part])
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnnotatedString.Builder.GetAppNameInAnnotatedBuilder(style: TextStyle = MaterialTheme.typography.displaySmallEmphasized) {
    withStyle(
        style = style.copy(
            color = MaterialTheme.colorScheme.primary
        ).toSpanStyle()
    ) {
        append(stringResource(id = R.string.app_name_first_part))
    }
    withStyle(style = style.toSpanStyle()) {
        append(stringResource(id = R.string.app_name_second_part))
    }
}

/**
 * It returns the app name as [Text] with the word "Libre" colored with the
 * primary color and with the [style] provided
 */
@Composable
fun AppNameText(style: TextStyle = LocalTextStyle.current) {
    Text(
        text = buildAnnotatedString {
            GetAppNameInAnnotatedBuilder(style)
        },
        style = style
    )
}
