/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.about

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.librefit.R

/**
 * What happens when an [AboutEntry] is activated.
 */
internal sealed interface AboutAction {
    /** The entry is informational and renders non-interactive. */
    data object None : AboutAction

    /** Navigates to the tutorial screen. */
    data object Tutorial : AboutAction

    /** Navigates to the privacy policy screen. */
    data object Privacy : AboutAction

    /** Navigates to the license screen. */
    data object License : AboutAction

    /** Navigates to the dependencies screen. */
    data object Dependencies : AboutAction

    /**
     * Opens the URL held by the given string resource.
     *
     * @param urlRes string resource whose value is the URL to open.
     */
    data class Url(@StringRes val urlRes: Int) : AboutAction
}

/**
 * A single row on the About screen.
 *
 * @param title string resource used as the row label.
 * @param description string resource parts rendered as the row subtitle: the first element is a
 * format pattern (e.g. `%1$s`), the remaining elements are its positional format arguments.
 * @param action what happens when the row is activated; [AboutAction.None] renders it non-interactive.
 * @param icon drawable resource shown at the start of the row.
 */
internal data class AboutEntry(
    @StringRes val title: Int,
    @StringRes val description: List<Int> = emptyList(),
    val action: AboutAction = AboutAction.None,
    @DrawableRes val icon: Int = R.drawable.ic_person,
)

/**
 * A titled group of [AboutEntry] rows rendered under a section headline.
 *
 * @param title string resource used as the section headline.
 * @param entries the rows of this section, in display order.
 */
internal data class AboutSection(
    @StringRes val title: Int,
    val entries: List<AboutEntry>,
)

/** Description parts rendering as "Contributed to: <language>" via the `%1$s` format pattern. */
private fun translatedTo(@StringRes language: Int): List<Int> =
    listOf(R.string.contributed_to_language, language)

/** Shorthand for a URL-opening action backed by a string resource. */
private fun url(@StringRes urlRes: Int): AboutAction = AboutAction.Url(urlRes)

private val infoEntries = listOf(
    AboutEntry(
        title = R.string.tutorial,
        description = listOf(R.string.tutorial_desc),
        action = AboutAction.Tutorial,
        icon = R.drawable.ic_help,
    ),
    AboutEntry(
        title = R.string.privacy,
        description = listOf(R.string.privacy_policy_desc),
        action = AboutAction.Privacy,
        icon = R.drawable.ic_policy,
    ),
    AboutEntry(R.string.website, action = url(R.string.url_website), icon = R.drawable.ic_globe),
    AboutEntry(
        title = R.string.license,
        description = listOf(R.string.license_desc),
        action = AboutAction.License,
        icon = R.drawable.ic_license,
    ),
    AboutEntry(
        title = R.string.github,
        description = listOf(R.string.source_code),
        action = url(R.string.url_source_code),
        icon = R.drawable.ic_github,
    ),
    AboutEntry(
        title = R.string.codeberg,
        description = listOf(R.string.source_code),
        action = url(R.string.url_source_code_codeberg),
        icon = R.drawable.ic_codeberg,
    ),
    AboutEntry(
        title = R.string.dependencies,
        action = AboutAction.Dependencies,
        icon = R.drawable.ic_contract,
    ),
    AboutEntry(R.string.IamDg, listOf(R.string.founder), url(R.string.url_IamDg)),
)

private val donatorEntries = listOf(
    AboutEntry(R.string.FlashyGhost),
    AboutEntry(R.string.jakedevs),
    AboutEntry(R.string.debianite65, action = url(R.string.url_debianite65)),
    AboutEntry(R.string.the_blue_blurr),
    AboutEntry(R.string.N4ü),
)

private val contributorEntries = listOf(
    AboutEntry(R.string.dpusceddu, action = url(R.string.url_dpusceddu)),
    AboutEntry(R.string.b3nj5m1n, action = url(R.string.url_b3nj5m1n)),
    AboutEntry(R.string.ByYeah, action = url(R.string.url_ByYeah)),
    AboutEntry(R.string.VanemKrAu, action = url(R.string.url_VanemKrAu)),
    AboutEntry(R.string.allddd, action = url(R.string.url_allddd)),
    AboutEntry(R.string.UbioZur, action = url(R.string.url_UbioZur)),
)

private val translatorEntries = listOf(
    AboutEntry(
        title = R.string.doen1el,
        description = translatedTo(R.string.language_german),
        action = url(R.string.url_doen1el),
    ),
    AboutEntry(
        title = R.string.kid1412621,
        description = translatedTo(R.string.language_chinese_simplified),
        action = url(R.string.url_kid1412621),
    ),
    AboutEntry(
        title = R.string.mwesten,
        description = translatedTo(R.string.language_dutch),
        action = url(R.string.url_mwesten),
    ),
    AboutEntry(R.string.Odweta, translatedTo(R.string.language_czech)),
    AboutEntry(
        title = R.string.VA5H_One,
        description = translatedTo(R.string.language_spanish),
        action = url(R.string.url_VA5H_One),
    ),
    AboutEntry(
        title = R.string.AhmedAwad7,
        description = translatedTo(R.string.language_arabic),
        action = url(R.string.url_AhmedAwad7),
    ),
    AboutEntry(
        title = R.string.b0saleh,
        description = translatedTo(R.string.language_arabic),
        action = url(R.string.url_b0saleh),
    ),
    AboutEntry(
        title = R.string.miguelsoaresouza8_droid,
        description = translatedTo(R.string.language_brazilian_portuguese),
        action = url(R.string.url_miguelsoaresouza8_droid),
    ),
    AboutEntry(
        title = R.string.DoubleCheddarBurger,
        description = translatedTo(R.string.language_catalan),
        action = url(R.string.url_DoubleCheddarBurger),
    ),
    AboutEntry(
        title = R.string.Spartang_117,
        description = translatedTo(R.string.language_french),
        action = url(R.string.url_Spartang_117),
    ),
    AboutEntry(R.string.josé_m, translatedTo(R.string.language_spanish)),
    AboutEntry(
        title = R.string.SilentCoderHere,
        description = translatedTo(R.string.language_hindi),
        action = url(R.string.url_SilentCoderHere),
    ),
    AboutEntry(
        title = R.string.raihankr,
        description = translatedTo(R.string.language_indonesian),
        action = url(R.string.url_raihankr),
    ),
    AboutEntry(
        title = R.string.SelwynDO,
        description = translatedTo(R.string.language_spanish),
        action = url(R.string.url_SelwynDO),
    ),
    AboutEntry(R.string.UnknownLi, translatedTo(R.string.language_ukrainian)),
    AboutEntry(
        title = R.string.xorodev,
        description = translatedTo(R.string.language_spanish),
        action = url(R.string.url_xorodev),
    ),
    AboutEntry(
        title = R.string.johncorea580_crypto,
        description = translatedTo(R.string.language_spanish),
        action = url(R.string.url_johncorea580_crypto),
    ),
    AboutEntry(R.string.theswordsgame, translatedTo(R.string.language_polish)),
    AboutEntry(
        title = R.string.ylconion,
        description = translatedTo(R.string.language_chinese_simplified),
        action = url(R.string.url_ylconion),
    ),
    AboutEntry(
        title = R.string.zei_dan,
        description = translatedTo(R.string.language_chinese_simplified),
        action = url(R.string.url_zei_dan),
    ),
    AboutEntry(
        title = R.string.Robin_Schanbacher,
        description = translatedTo(R.string.language_german),
        action = url(R.string.url_Robin_Schanbacher),
    ),
    AboutEntry(
        title = R.string.sprivaq,
        description = translatedTo(R.string.language_german),
        action = url(R.string.url_sprivaq),
    ),
    AboutEntry(
        title = R.string.Safi_Ullah,
        description = translatedTo(R.string.language_urdu),
        action = url(R.string.url_Safi_Ullah),
    ),
    AboutEntry(
        title = R.string.dzjulis,
        description = translatedTo(R.string.language_russian),
        action = url(R.string.url_dzjulis),
    ),
    AboutEntry(R.string.Jae_Hyuk_Lee, translatedTo(R.string.language_korean)),
    AboutEntry(
        title = R.string.Semprista,
        description = translatedTo(R.string.language_french),
        action = url(R.string.url_Semprista),
    ),
    AboutEntry(
        title = R.string.mister_bum,
        description = translatedTo(R.string.language_german),
        action = url(R.string.url_mister_bum),
    ),
    AboutEntry(
        title = R.string.BigP0tato,
        description = translatedTo(R.string.language_french),
        action = url(R.string.url_BigP0tato),
    ),
    AboutEntry(
        title = R.string.T_Silverspoon,
        description = translatedTo(R.string.language_swedish),
        action = url(R.string.url_T_Silverspoon),
    ),
    AboutEntry(R.string.tomel51733, translatedTo(R.string.language_spanish)),
    AboutEntry(
        title = R.string.Ahmedbd23,
        description = translatedTo(R.string.language_chinese_simplified),
        action = url(R.string.url_Ahmedbd23),
    ),
    AboutEntry(
        title = R.string.Jent1357,
        description = translatedTo(R.string.language_german),
        action = url(R.string.url_Jent1357),
    ),
    AboutEntry(
        title = R.string.zhx000,
        description = translatedTo(R.string.language_german),
        action = url(R.string.url_zhx000),
    ),
    AboutEntry(
        title = R.string.UbioZur,
        description = translatedTo(R.string.language_french),
        action = url(R.string.url_UbioZur),
    ),
    AboutEntry(
        title = R.string.malespiaut,
        description = translatedTo(R.string.language_french),
        action = url(R.string.url_malespiaut),
    ),
    AboutEntry(
        title = R.string.alexis_charp,
        description = translatedTo(R.string.language_french),
        action = url(R.string.url_alexis_charp),
    ),
    AboutEntry(R.string.Miles_Espadoto, translatedTo(R.string.language_brazilian_portuguese)),
    AboutEntry(
        title = R.string.vinitdrk,
        description = translatedTo(R.string.language_brazilian_portuguese),
        action = url(R.string.url_vinitdrk),
    ),
    AboutEntry(R.string.Jahn_Andreas, translatedTo(R.string.language_swedish)),
    AboutEntry(
        title = R.string.hyperion51,
        description = translatedTo(R.string.language_spanish),
        action = url(R.string.url_hyperion51),
    ),
    AboutEntry(
        title = R.string.lotation,
        description = translatedTo(R.string.language_italian),
        action = url(R.string.url_lotation),
    ),
    AboutEntry(
        title = R.string.joncasagrande,
        description = translatedTo(R.string.language_portuguese),
        action = url(R.string.url_joncasagrande),
    ),
    AboutEntry(
        title = R.string.WinnerWind,
        description = translatedTo(R.string.language_hindi),
        action = url(R.string.url_WinnerWind),
    ),
    AboutEntry(R.string.Rajesh_Ranmara, translatedTo(R.string.language_hindi)),
)

/**
 * The ordered sections of the About screen: headline string resource plus its entries.
 * Section and entry order match the hand-picked order of the original About screen.
 */
internal val aboutSections: List<AboutSection> = listOf(
    AboutSection(R.string.info, infoEntries),
    AboutSection(R.string.donators, donatorEntries),
    AboutSection(R.string.contributors, contributorEntries),
    AboutSection(R.string.translators, translatorEntries),
)