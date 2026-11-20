/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.enums.healthConnect

enum class HealthConnectSyncOption(
    val preferenceId: String,
    val accessMode: HealthConnectAccessMode
) {
    WEIGHT_READ("weight_read", HealthConnectAccessMode.READ),
    WEIGHT_WRITE("weight_write", HealthConnectAccessMode.WRITE),
    FAT_READ("fat_read", HealthConnectAccessMode.READ),
    FAT_WRITE("fat_write", HealthConnectAccessMode.WRITE),
    WORKOUT_WRITE("workout_write", HealthConnectAccessMode.WRITE)
}

enum class HealthConnectAccessMode {
    READ,
    WRITE
}
