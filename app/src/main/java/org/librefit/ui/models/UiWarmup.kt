/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 *
 */

package org.librefit.ui.models

import androidx.compose.runtime.Immutable
import org.librefit.enums.WarmupMode
import org.librefit.ui.models.mappers.toEntity
import kotlin.random.Random


/**
 * The [org.librefit.db.entity.Warmup] model used only by the ui.
 * Conversion to the db is model is handled by [UiWarmup.toEntity]
 *
 * @see [org.librefit.db.entity.Warmup]
 */
@Immutable
data class UiWarmup(
    val id: Long = Random.nextLong(),
    val notes: String = "",
    val warmupMode: WarmupMode = WarmupMode.DEFAULT,
    val restTime: Int = 0,
    val position: Int = 0,
    val target: Double = .0,
    val workoutId: Long = 0
)
