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

object LibreFitAppName {
    /**
     * It returns the app name with material theme style and with the word "Libre" colored with the
     * primary color
     */
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun AnnotatedString.Builder.GetAppNameInAnnotatedBuilder(style: TextStyle = MaterialTheme.typography.displaySmallEmphasized) {
        withStyle(
            style = style.copy(
                color = MaterialTheme.colorScheme.primary
            ).toSpanStyle()
        ) {
            append(stringResource(id = R.string.app_name).removeRange(5, 8))
        }
        withStyle(style = style.toSpanStyle()) {
            append(stringResource(id = R.string.app_name).removeRange(0, 5))
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
}