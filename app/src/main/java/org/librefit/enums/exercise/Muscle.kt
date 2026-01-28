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
enum class Muscle : ExerciseProperty {
    @SerialName(value = "abdominals")
    ABDOMINALS(),

    @SerialName(value = "abductors")
    ABDUCTORS(),

    @SerialName(value = "adductors")
    ADDUCTORS(),

    @SerialName(value = "biceps")
    BICEPS(),

    @SerialName(value = "calves")
    CALVES(),

    @SerialName(value = "chest")
    CHEST(),

    @SerialName(value = "forearms")
    FOREARMS(),

    @SerialName(value = "glutes")
    GLUTES(),

    @SerialName(value = "hamstrings")
    HAMSTRINGS(),

    @SerialName(value = "lats")
    LATS(),

    @SerialName(value = "lower back")
    LOWER_BACK(),

    @SerialName(value = "middle back")
    MIDDLE_BACK(),

    @SerialName(value = "neck")
    NECK(),

    @SerialName(value = "quadriceps")
    QUADRICEPS(),

    @SerialName(value = "shoulders")
    SHOULDERS(),

    @SerialName(value = "traps")
    TRAPS(),

    @SerialName(value = "triceps")
    TRICEPS();
}