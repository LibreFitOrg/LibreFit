/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.nav

import android.content.Intent

/**
 * Maps an incoming [Intent] action name to the navigation key it should open.
 *
 * This is the single place where system-originated entry points are bound to in-app
 * destinations. It operates on the action name as a plain string so the mapping stays pure and
 * unit-testable without Android framework dependencies. See also `AndroidManifest.xml`, where
 * [Intent.ACTION_APPLICATION_PREFERENCES] is declared on `MainActivity` so the system Settings
 * app (App Info page) offers the in-app Settings screen.
 *
 * @receiver the action name of an incoming Intent, e.g. [Intent.ACTION_APPLICATION_PREFERENCES],
 *   or `null` when the intent carries no action.
 * @return the [Route] to open for the receiver action, or `null` when it is not a supported
 *   entry point.
 */
fun String?.deepLinkKeyForAction(): Route? = when (this) {
    Intent.ACTION_APPLICATION_PREFERENCES -> Route.SettingsScreen
    else -> null
}
