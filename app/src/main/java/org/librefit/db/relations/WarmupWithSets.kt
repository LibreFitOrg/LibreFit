/*
 *
 *  * SPDX-License-Identifier: GPL-3.0-or-later
 *  * Copyright (c) 2025-2026. The LibreFit Contributors
 *  *
 *  * LibreFit is subject to additional terms covering author attribution and trademark usage;
 *  * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 *
 */

package org.librefit.db.relations

import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.serialization.Serializable
import org.librefit.db.entity.Set
import org.librefit.db.entity.Warmup

/**
 * A data class representing a [Warmup] with its associated [Set]s.
 *
 * This class is used by Room to retrieve all the data associated with a warmup and
 * the sets associated with it.
 *
 * @property warmup It contains the user related data associated with this [Warmup].
 * @property sets The list of [Set] associated with the [warmup] containing all the user related data.
 */
@Serializable
data class WarmupWithSets(
    @Embedded val warmup: Warmup = Warmup(),
    @Relation(
        parentColumn = "id",
        entityColumn = "warmupId"
    )
    val sets: List<Set> = listOf(Set()),
)
