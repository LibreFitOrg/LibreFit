/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.enums.exercise

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Level : ExerciseProperty {
    @SerialName(value = "beginner")
    BEGINNER,

    @SerialName(value = "intermediate")
    INTERMEDIATE,

    @SerialName(value = "expert")
    EXPERT
}