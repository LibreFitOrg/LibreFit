/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.nav

import android.content.Intent
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for [deepLinkKeyForAction], the pure mapper binding system-originated intent
 * actions to in-app navigation keys.
 */
class DeepLinksTest {

    @Test
    fun `deepLinkKeyForAction maps the application preferences action to the settings screen`() {
        assertThat(Intent.ACTION_APPLICATION_PREFERENCES.deepLinkKeyForAction())
            .isEqualTo(Route.SettingsScreen)
    }

    @Test
    fun `deepLinkKeyForAction ignores the launcher main action`() {
        assertThat(Intent.ACTION_MAIN.deepLinkKeyForAction()).isNull()
    }

    @Test
    fun `deepLinkKeyForAction ignores unknown actions`() {
        assertThat("com.example.UNKNOWN_ACTION".deepLinkKeyForAction()).isNull()
    }

    @Test
    fun `deepLinkKeyForAction ignores a null action`() {
        val action: String? = null
        assertThat(action.deepLinkKeyForAction()).isNull()
    }
}
