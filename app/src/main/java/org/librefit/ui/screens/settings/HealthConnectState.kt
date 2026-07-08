/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.settings

data class HealthConnectState(
    val status: HealthConnectStatus = HealthConnectStatus.UNAVAILABLE,
    val isEnabled: Boolean = false,
    val exportedRecords: Int? = null
) {
    val isAvailable: Boolean
        get() = status != HealthConnectStatus.UNAVAILABLE

    val hasPermissions: Boolean
        get() = status == HealthConnectStatus.READY ||
            status == HealthConnectStatus.EXPORTING

    val isExporting: Boolean
        get() = status == HealthConnectStatus.EXPORTING

    val isChecked: Boolean
        get() = isEnabled && hasPermissions
}

enum class HealthConnectStatus {
    UNAVAILABLE,
    NEEDS_PERMISSIONS,
    READY,
    EXPORTING
}

enum class HealthConnectClickAction {
    NONE,
    REQUEST_PERMISSIONS
}
