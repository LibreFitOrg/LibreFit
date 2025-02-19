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

package org.librefit.data

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import org.librefit.enums.exercise.Force
import org.librefit.enums.exercise.Level
import org.librefit.enums.exercise.Mechanic
import org.librefit.enums.exercise.Muscle
import org.librefit.util.ExerciseDC
import java.lang.reflect.Type
import java.util.Locale

class ExerciseDeserializer : JsonDeserializer<ExerciseDC> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ExerciseDC {
        val jsonObject = json.asJsonObject

        return ExerciseDC(
            id = jsonObject["id"].asString,
            name = jsonObject["name"].asString,
            force = if (jsonObject["force"].isJsonNull) {
                null // Handle null case for force
            } else {
                Force.valueOf(jsonObject["force"].asString.uppercase())
            },
            level = Level.valueOf(jsonObject["level"].asString.uppercase()),
            mechanic = if (jsonObject["mechanic"].isJsonNull) {
                null // Handle null case
            } else {
                Mechanic.valueOf(jsonObject["mechanic"].asString.uppercase())
            },
            equipment = if (jsonObject["equipment"].isJsonNull) {
                null // Handle null case for equipment
            } else {
                when (jsonObject["equipment"].asString) {
                    "body only" -> Equipment.BODY_ONLY
                    "medicine ball" -> Equipment.MEDICINE_BALL
                    "dumbbell" -> Equipment.DUMBBELL
                    "bands" -> Equipment.BANDS
                    "kettlebells" -> Equipment.KETTLEBELLS
                    "foam roll" -> Equipment.FOAM_ROLL
                    "cable" -> Equipment.CABLE
                    "machine" -> Equipment.MACHINE
                    "barbell" -> Equipment.BARBELL
                    "exercise ball" -> Equipment.EXERCISE_BALL
                    "e-z curl bar" -> Equipment.E_Z_CURL_BAR
                    "other" -> Equipment.OTHER
                    else -> null // Handle unexpected values
                }
            },
            primaryMuscles = if (jsonObject["primaryMuscles"].isJsonNull) {
                emptyList() // Return an empty list if null
            } else {
                val muscleList = jsonObject["primaryMuscles"].asJsonArray
                muscleList.mapNotNull { muscleElement ->
                    val muscleName = muscleElement.asString.uppercase(Locale.ROOT)
                        .replace(" ", "_") // Convert to enum format
                    Muscle.entries.find { it.name == muscleName } // Find the enum value
                }
            },
            secondaryMuscles = if (jsonObject["secondaryMuscles"].isJsonNull) {
                emptyList() // Return an empty list if null
            } else {
                val muscleList = jsonObject["secondaryMuscles"].asJsonArray
                muscleList.mapNotNull { muscleElement ->
                    val muscleName = muscleElement.asString.uppercase(Locale.ROOT)
                        .replace(" ", "_") // Convert to enum format
                    Muscle.entries.find { it.name == muscleName } // Find the enum value
                }
            },
            instructions = context.deserialize(jsonObject["instructions"], List::class.java),
            category = when (jsonObject["category"].asString) {
                "powerlifting" -> Category.POWERLIFTING
                "strength" -> Category.STRENGTH
                "stretching" -> Category.STRETCHING
                "cardio" -> Category.CARDIO
                "olympic weightlifting" -> Category.OLYMPIC_WEIGHTLIFTING
                "strongman" -> Category.STRONGMAN
                "plyometrics" -> Category.PLYOMETRICS
                else -> throw IllegalArgumentException("Unknown category: ${jsonObject["category"].asString}")
            },
            images = context.deserialize(jsonObject["images"], List::class.java)
        )
    }
}
