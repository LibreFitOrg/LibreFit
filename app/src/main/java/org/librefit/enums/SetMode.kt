/*
 * Copyright (c) 2024-2025. LibreFit
 *
 * This file is part of LibreFit
 *
 * LibreFit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LibreFit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LibreFit.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.librefit.enums

import org.librefit.enums.SetMode.DURATION
import org.librefit.enums.SetMode.LOAD_AND_BODY_WEIGHT
import org.librefit.enums.SetMode.LOAD_ONLY
import org.librefit.enums.SetMode.REPS


/**
 * Enum representing the mode for the [org.librefit.db.entity.Set]s of an [org.librefit.db.entity.Exercise].
 *
 * @property LOAD_ONLY            A set that uses an external load only (e.g., barbell squat, dumbbell curl).
 * @property REPS                 A set measured by a count of repetitions (commonly body-weight or un-loaded exercises).
 * @property LOAD_AND_BODY_WEIGHT A set that combines an external load with the user’s body weight (e.g., weighted pull-ups, weighted dips).
 * @property DURATION             A set measured by elapsed time rather (e.g., plank, jumping jacks).
 */
enum class SetMode {
    LOAD_ONLY,
    REPS,
    LOAD_AND_BODY_WEIGHT,
    DURATION
}