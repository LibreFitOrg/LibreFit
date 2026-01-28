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
enum class Equipment : ExerciseProperty {
    @SerialName(value = "medicine ball")
    MEDICINE_BALL,

    @SerialName(value = "dumbbell")
    DUMBBELL,

    @SerialName(value = "body only")
    BODY_ONLY,

    @SerialName(value = "bands")
    BANDS,

    @SerialName(value = "kettlebells")
    KETTLEBELLS,

    @SerialName(value = "foam roll")
    FOAM_ROLL,

    @SerialName(value = "cable")
    CABLE,

    @SerialName(value = "machine")
    MACHINE,

    @SerialName(value = "barbell")
    BARBELL,

    @SerialName(value = "exercise ball")
    EXERCISE_BALL,

    @SerialName(value = "e-z curl bar")
    E_Z_CURL_BAR,

    @SerialName(value = "other")
    OTHER
}