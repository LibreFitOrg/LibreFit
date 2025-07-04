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

enum class Equipment : ExerciseProperty {
    @Json(name = "medicine ball")
    MEDICINE_BALL,
    @Json(name = "dumbbell")
    DUMBBELL,
    @Json(name = "body only")
    BODY_ONLY,
    @Json(name = "bands")
    BANDS,
    @Json(name = "kettlebells")
    KETTLEBELLS,
    @Json(name = "foam roll")
    FOAM_ROLL,
    @Json(name = "cable")
    CABLE,
    @Json(name = "machine")
    MACHINE,
    @Json(name = "barbell")
    BARBELL,
    @Json(name = "exercise ball")
    EXERCISE_BALL,
    @Json(name = "e-z curl bar")
    E_Z_CURL_BAR,
    @Json(name = "other")
    OTHER
}