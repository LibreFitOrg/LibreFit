/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import org.librefit.enums.exercise.Force
import org.librefit.enums.exercise.Level
import org.librefit.enums.exercise.Mechanic
import org.librefit.enums.exercise.Muscle

/**
 * This data class stores exercises as parsed from `res/raw/exercises.json`. The dataset in provided by [org.librefit.db.repository.DatasetRepository]
 * `kotlin.serialization` is used for JSON serialization and deserialization as indicated by
 * [Serializable] annotation. The actual exercise entries in database are handled by [Exercise]
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
 * @property force The [org.librefit.enums.exercise.Force] type applied during the exercise. Acceptable values are defined in the JSON schema.
 * @property level The difficulty [org.librefit.enums.exercise.Level] of the exercise. Acceptable values are defined in the JSON schema.
 * @property mechanic The exercise [org.librefit.enums.exercise.Mechanic]. Acceptable values are defined in the JSON schema.
 * @property equipment The [org.librefit.enums.exercise.Equipment] required for the exercise. Acceptable values are defined in the JSON schema.
 * @property primaryMuscles List of primary [org.librefit.enums.exercise.Muscle]s involved in the exercise. Acceptable values are defined in the JSON schema.
 * @property secondaryMuscles List of secondary [org.librefit.enums.exercise.Muscle]s involved in the exercise. Acceptable values are defined in the JSON schema.
 * @property instructions Step-by-step instructions detailing how to perform the exercise.
 * @property category The [org.librefit.enums.exercise.Category] of the exercise. Acceptable values are defined in the JSON schema.
 * @property images Identifiers of images associated with the exercise.
 * @property isCustomExercise It tells whether this exercise is created by the user or not. By default,
 * the exercise comes from `exercises.json` so it is set to `false`.
 */
@Serializable
@Entity(tableName = "dataset")
data class ExerciseDC(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val force: Force? = null,
    val level: Level = Level.BEGINNER,
    val mechanic: Mechanic? = null,
    val equipment: Equipment? = null,
    val primaryMuscles: List<Muscle> = listOf(),
    val secondaryMuscles: List<Muscle> = listOf(),
    val instructions: List<String> = listOf(),
    val category: Category = Category.POWERLIFTING,
    val images: List<String> = listOf(),
    val isCustomExercise: Boolean = false
)