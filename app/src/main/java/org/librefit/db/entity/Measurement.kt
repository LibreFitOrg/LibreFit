/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.db.entity

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Entity(tableName = "measurements")
@Serializable
data class Measurement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @get:FloatRange(0.0, 300.0) val bodyWeight: Double = 0.0,
    @get:IntRange(0, 100) val bodyFatPercentage: Int = 0,
    @get:IntRange(0, 100) val muscleMassPercentage: Int = 0,
    @Serializable(with = LocalDateTimeSerializer::class)
    val date: LocalDateTime = LocalDateTime.now(),
    val notes: String = ""
)
