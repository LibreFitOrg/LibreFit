/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.models

import org.librefit.enums.healthConnect.HealthConnectSyncOption

data class HealthConnectState(
    val isAvailable: Boolean = false,
    val isEnabled: Boolean = false,
    val grantedPermissions: Set<String> = emptySet(),
    val enabledOptions: Set<HealthConnectSyncOption> = emptySet()
)
