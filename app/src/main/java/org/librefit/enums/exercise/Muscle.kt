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

package org.librefit.enums.exercise

import com.squareup.moshi.Json

enum class Muscle {
    @Json(name = "abdominals")
    ABDOMINALS(),
    @Json(name = "abductors")
    ABDUCTORS(),
    @Json(name = "adductors")
    ADDUCTORS(),
    @Json(name = "biceps")
    BICEPS(),
    @Json(name = "calves")
    CALVES(),
    @Json(name = "chest")
    CHEST(),
    @Json(name = "forearms")
    FOREARMS(),
    @Json(name = "glutes")
    GLUTES(),
    @Json(name = "hamstrings")
    HAMSTRINGS(),
    @Json(name = "lats")
    LATS(),
    @Json(name = "lower back")
    LOWER_BACK(),
    @Json(name = "middle back")
    MIDDLE_BACK(),
    @Json(name = "neck")
    NECK(),
    @Json(name = "quadriceps")
    QUADRICEPS(),
    @Json(name = "shoulders")
    SHOULDERS(),
    @Json(name = "traps")
    TRAPS(),
    @Json(name = "triceps")
    TRICEPS();
}