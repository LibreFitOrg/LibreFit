/*
 * Copyright (c) 2024-2025. LibreFit
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

package org.librefit.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.tooling.preview.Preview
import org.librefit.R
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold


@OptIn(ExperimentalTextApi::class)
val bodyFontFamily = FontFamily(
    Font(
        resId = R.font.roboto_flex_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.width(125f),
            FontVariation.weight(200),
            FontVariation.grade(200),
        )
    )
)


@OptIn(ExperimentalTextApi::class)
val bodyFontFamilyEmphasized = FontFamily(
    Font(
        resId = R.font.roboto_flex_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.width(125f),
            FontVariation.weight(400),
            FontVariation.grade(200),
            FontVariation.Setting("XOPQ", 96F),
            FontVariation.Setting("XTRA", 500F),
            FontVariation.Setting("YOPQ", 79F),
            FontVariation.Setting("YTAS", 750F),
            FontVariation.Setting("YTDE", -203F),
            FontVariation.Setting("YTFI", 738F),
            FontVariation.Setting("YTLC", 514F),
            FontVariation.Setting("YTUC", 712F)
        )
    )
)


@OptIn(ExperimentalTextApi::class)
val displayFontFamily = FontFamily(
    Font(
        resId = R.font.roboto_flex_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.width(125f),
            FontVariation.weight(600),
            FontVariation.grade(200),
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val displayFontFamilyEmphasized = FontFamily(
    Font(
        resId = R.font.roboto_flex_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.width(125f),
            FontVariation.weight(800),
            FontVariation.grade(400),
            FontVariation.Setting("XOPQ", 100F),
            FontVariation.Setting("XTRA", 550F),
            FontVariation.Setting("YOPQ", 80F),
            FontVariation.Setting("YTAS", 750F),
            FontVariation.Setting("YTDE", -200F),
            FontVariation.Setting("YTFI", 700F),
            FontVariation.Setting("YTLC", 550F),
            FontVariation.Setting("YTUC", 712F)
        )
    )
)


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val baseline = Typography()


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = displayFontFamily),
    displayMedium = baseline.displayMedium.copy(fontFamily = displayFontFamily),
    displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = displayFontFamily),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = displayFontFamily),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = displayFontFamily),
    titleLarge = baseline.titleLarge.copy(fontFamily = displayFontFamily),
    titleMedium = baseline.titleMedium.copy(fontFamily = displayFontFamily),
    titleSmall = baseline.titleSmall.copy(fontFamily = displayFontFamily),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily),
    bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily),
    labelLarge = baseline.labelLarge.copy(fontFamily = bodyFontFamily),
    labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily),
    labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily),
    displayLargeEmphasized = baseline.displayLargeEmphasized.copy(fontFamily = displayFontFamilyEmphasized),
    displayMediumEmphasized = baseline.displayMediumEmphasized.copy(fontFamily = displayFontFamilyEmphasized),
    displaySmallEmphasized = baseline.displaySmallEmphasized.copy(fontFamily = displayFontFamilyEmphasized),
    headlineLargeEmphasized = baseline.headlineLargeEmphasized.copy(fontFamily = displayFontFamilyEmphasized),
    headlineMediumEmphasized = baseline.headlineMediumEmphasized.copy(fontFamily = displayFontFamilyEmphasized),
    headlineSmallEmphasized = baseline.headlineSmallEmphasized.copy(fontFamily = displayFontFamilyEmphasized),
    titleLargeEmphasized = baseline.titleLargeEmphasized.copy(fontFamily = displayFontFamilyEmphasized),
    titleMediumEmphasized = baseline.titleMediumEmphasized.copy(fontFamily = displayFontFamilyEmphasized),
    titleSmallEmphasized = baseline.titleSmallEmphasized.copy(fontFamily = displayFontFamilyEmphasized),
    bodyLargeEmphasized = baseline.bodyLargeEmphasized.copy(fontFamily = bodyFontFamilyEmphasized),
    bodyMediumEmphasized = baseline.bodyMediumEmphasized.copy(fontFamily = bodyFontFamilyEmphasized),
    bodySmallEmphasized = baseline.bodySmallEmphasized.copy(fontFamily = bodyFontFamilyEmphasized),
    labelLargeEmphasized = baseline.labelLargeEmphasized.copy(fontFamily = bodyFontFamilyEmphasized),
    labelMediumEmphasized = baseline.labelMediumEmphasized.copy(fontFamily = bodyFontFamilyEmphasized),
    labelSmallEmphasized = baseline.labelSmallEmphasized.copy(fontFamily = bodyFontFamilyEmphasized),
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
private fun TypographyPreview() {
    val showOnlyEmphasized: Boolean? = true
    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        LibreFitScaffold { innerPadding ->
            LibreFitLazyColumn(innerPadding = innerPadding) {
                if (showOnlyEmphasized != true) {
                    item {
                        Text(
                            text = "displayLarge",
                            style = MaterialTheme.typography.displayLarge
                        )
                    }
                    item {
                        Text(
                            text = "displayMedium",
                            style = MaterialTheme.typography.displayMedium
                        )
                    }
                    item {
                        Text(
                            text = "displaySmall",
                            style = MaterialTheme.typography.displaySmall
                        )
                    }
                    item {
                        Text(
                            text = "headlineLarge",
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                    item {
                        Text(
                            text = "headlineMedium",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    item {
                        Text(
                            text = "headlineSmall",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    item {
                        Text(
                            text = "titleLarge",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    item {
                        Text(
                            text = "titleMedium",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    item {
                        Text(
                            text = "titleSmall",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    item {
                        Text(
                            text = "bodyLarge",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    item {
                        Text(
                            text = "bodyMedium",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    item {
                        Text(
                            text = "bodySmall",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (showOnlyEmphasized != false) {
                    item {
                        Text(
                            text = "displayLargeE",
                            style = MaterialTheme.typography.displayLargeEmphasized
                        )
                    }
                    item {
                        Text(
                            text = "displayMediumE",
                            style = MaterialTheme.typography.displayMediumEmphasized
                        )
                    }
                    item {
                        Text(
                            text = "displaySmallE",
                            style = MaterialTheme.typography.displaySmallEmphasized
                        )
                    }
                    item {
                        Text(
                            text = "headlineLargeE",
                            style = MaterialTheme.typography.headlineLargeEmphasized
                        )
                    }
                    item {
                        Text(
                            text = "headlineMediumE",
                            style = MaterialTheme.typography.headlineMediumEmphasized
                        )
                    }
                    item {
                        Text(
                            text = "headlineSmallE",
                            style = MaterialTheme.typography.headlineSmallEmphasized
                        )
                    }
                    item {
                        Text(
                            text = "titleLargeE",
                            style = MaterialTheme.typography.titleLargeEmphasized
                        )
                    }
                    item {
                        Text(
                            text = "titleMediumE",
                            style = MaterialTheme.typography.titleMediumEmphasized
                        )
                    }
                    item {
                        Text(
                            text = "titleSmallE",
                            style = MaterialTheme.typography.titleSmallEmphasized
                        )
                    }
                    item {
                        Text(
                            text = "bodyLargeE",
                            style = MaterialTheme.typography.bodyLargeEmphasized
                        )
                    }
                    item {
                        Text(
                            text = "bodyMediumE",
                            style = MaterialTheme.typography.bodyMediumEmphasized
                        )
                    }
                    item {
                        Text(
                            text = "bodySmallE",
                            style = MaterialTheme.typography.bodySmallEmphasized
                        )
                    }
                }
            }
        }
    }
}