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

import com.squareup.moshi.JsonClass
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import org.librefit.enums.exercise.Force
import org.librefit.enums.exercise.Level
import org.librefit.enums.exercise.Mechanic
import org.librefit.enums.exercise.Muscle

/**
 * This class is used to store exercises as parsed from `res/raw/exercises.json` in
 * [org.librefit.ui.screens.shared.SharedViewModel].
 * Moshi's generated adapter is used for JSON serialization and deserialization as indicated by
 * [JsonClass] annotation. The actual exercise entries in database are handled by [org.librefit.db.Exercise]
 *
 * The JSON schema associated with this class is defined as follows:
 *
 * ```json
 * {
 *   "$schema": "http://json-schema.org/draft-04/schema#",
 *   "type": "object",
 *   "properties": {
 *     "id": {
 *       "type": "string",
 *       "pattern": "^[0-9a-zA-Z_-]+$"
 *     },
 *     "name": {
 *       "type": "string"
 *     },
 *     "force": {
 *       "type": [ "string", "null" ],
 *       "enum": [
 *         null,
 *         "static",
 *         "pull",
 *         "push"
 *       ]
 *     },
 *     "level": {
 *       "type": "string",
 *       "enum": [
 *         "beginner",
 *         "intermediate",
 *         "expert"
 *       ]
 *     },
 *     "mechanic": {
 *       "type": [ "string", "null" ],
 *       "enum": [
 *         "isolation",
 *         "compound",
 *         null
 *       ]
 *     },
 *     "equipment": {
 *       "type": [ "string", "null" ],
 *       "enum": [
 *         null,
 *         "medicine ball",
 *         "dumbbell",
 *         "body only",
 *         "bands",
 *         "kettlebells",
 *         "foam roll",
 *         "cable",
 *         "machine",
 *         "barbell",
 *         "exercise ball",
 *         "e-z curl bar",
 *         "other"
 *       ]
 *     },
 *     "primaryMuscles": {
 *       "type": "array",
 *       "items": [
 *         {
 *           "type": "string",
 *           "enum": [
 *             "abdominals",
 *             "abductors",
 *             "adductors",
 *             "biceps",
 *             "calves",
 *             "chest",
 *             "forearms",
 *             "glutes",
 *             "hamstrings",
 *             "lats",
 *             "lower back",
 *             "middle back",
 *             "neck",
 *             "quadriceps",
 *             "shoulders",
 *             "traps",
 *             "triceps"
 *           ]
 *         }
 *       ]
 *     },
 *     "secondaryMuscles": {
 *       "type": "array",
 *       "items": [
 *         {
 *           "type": "string",
 *           "enum": [
 *             "abdominals",
 *             "abductors",
 *             "adductors",
 *             "biceps",
 *             "calves",
 *             "chest",
 *             "forearms",
 *             "glutes",
 *             "hamstrings",
 *             "lats",
 *             "lower back",
 *             "middle back",
 *             "neck",
 *             "quadriceps",
 *             "shoulders",
 *             "traps",
 *             "triceps"
 *           ]
 *         }
 *       ]
 *     },
 *     "instructions": {
 *       "type": "array",
 *       "items": [{ "type": "string" }]
 *     },
 *     "category": {
 *       "type": "string",
 *       "enum": [
 *         "powerlifting",
 *         "strength",
 *         "stretching",
 *         "cardio",
 *         "olympic weightlifting",
 *         "strongman",
 *         "plyometrics"
 *       ]
 *     },
 *     "images": {
 *       "type": "array",
 *       "items": [{ "type": "string" }]
 *     }
 *   },
 *   "required": [
 *     "id",
 *     "name",
 *     "level",
 *     "mechanic",
 *     "equipment",
 *     "primaryMuscles",
 *     "secondaryMuscles",
 *     "instructions",
 *     "category",
 *     "images"
 *   ]
 * }
 * ```
 *
 *
 * @property id Unique identifier for the exercise (e.g., "Pull-up"). Must match the pattern "^[0-9a-zA-Z_-]+$".
 * @property name Name of the exercise.
 * @property force The [Force] type applied during the exercise. Acceptable values are defined in the JSON schema.
 * @property level The difficulty [Level] of the exercise. Acceptable values are defined in the JSON schema.
 * @property mechanic The exercise [Mechanic]. Acceptable values are defined in the JSON schema.
 * @property equipment The [Equipment] required for the exercise. Acceptable values are defined in the JSON schema.
 * @property primaryMuscles List of primary [Muscle]s involved in the exercise. Acceptable values are defined in the JSON schema.
 * @property secondaryMuscles List of secondary [Muscle]s involved in the exercise. Acceptable values are defined in the JSON schema.
 * @property instructions Step-by-step instructions detailing how to perform the exercise.
 * @property category The [Category] of the exercise. Acceptable values are defined in the JSON schema.
 * @property images Identifiers of images associated with the exercise.
 */
@JsonClass(generateAdapter = true)
data class ExerciseDC(
    val id: String = "",
    val name: String = "",
    val force: Force? = null,
    val level: Level = Level.BEGINNER,
    val mechanic: Mechanic? = null,
    val equipment: Equipment? = null,
    val primaryMuscles: List<Muscle> = listOf(),
    val secondaryMuscles: List<Muscle> = listOf(),
    val instructions: List<String> = listOf(),
    val category: Category = Category.POWERLIFTING,
    val images: List<String> = listOf()
)

