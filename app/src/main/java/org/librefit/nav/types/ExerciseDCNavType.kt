/*
 * Copyright (c) 2025. LibreFit
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

package org.librefit.nav.types

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.json.Json
import org.librefit.db.entity.ExerciseDC

class ExerciseDCNavType : NavType<ExerciseDC>(isNullableAllowed = false) {

    override fun get(bundle: Bundle, key: String): ExerciseDC? {
        val json = bundle.getString(key)
        return json?.let { Json.decodeFromString(it) }
    }

    override fun parseValue(value: String): ExerciseDC {
        val decoded = Uri.decode(value)
        return Json.Default.decodeFromString(decoded)
    }

    override fun put(bundle: Bundle, key: String, value: ExerciseDC) {
        val json = Json.Default.encodeToString(value)
        bundle.putString(key, json)
    }

    override fun serializeAsValue(value: ExerciseDC): String {
        val json = Json.Default.encodeToString(value)
        return Uri.encode(json)
    }
}