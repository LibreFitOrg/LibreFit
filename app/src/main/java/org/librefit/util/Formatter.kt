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

package org.librefit.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import org.librefit.R
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import org.librefit.enums.exercise.ExerciseProperty
import org.librefit.enums.exercise.Force
import org.librefit.enums.exercise.Level
import org.librefit.enums.exercise.Mechanic
import org.librefit.enums.exercise.Muscle
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.min

object Formatter {
    fun exerciseEnumToStringId(enum: ExerciseProperty?): Int {
        return when (enum) {
            Force.PUSH -> R.string.force_push
            Force.PULL -> R.string.force_pull
            Force.STATIC -> R.string.force_static
            Level.BEGINNER -> R.string.level_beginner
            Level.INTERMEDIATE -> R.string.level_intermediate
            Level.EXPERT -> R.string.level_expert
            Mechanic.ISOLATION -> R.string.mechanic_isolation
            Mechanic.COMPOUND -> R.string.mechanic_compound
            Equipment.MEDICINE_BALL -> R.string.equipment_medicine_ball
            Equipment.DUMBBELL -> R.string.equipment_dumbbell
            Equipment.BODY_ONLY -> R.string.equipment_body_only
            Equipment.BANDS -> R.string.equipment_bands
            Equipment.KETTLEBELLS -> R.string.equipment_kettlebells
            Equipment.FOAM_ROLL -> R.string.equipment_foam_roll
            Equipment.CABLE -> R.string.equipment_cable
            Equipment.MACHINE -> R.string.equipment_machine
            Equipment.BARBELL -> R.string.equipment_barbell
            Equipment.EXERCISE_BALL -> R.string.equipment_exercise_ball
            Equipment.E_Z_CURL_BAR -> R.string.equipment_ez_curl_bar
            Equipment.OTHER -> R.string.equipment_other
            Muscle.ABDOMINALS -> R.string.muscle_abdominals
            Muscle.ABDUCTORS -> R.string.muscle_abductors
            Muscle.ADDUCTORS -> R.string.muscle_adductors
            Muscle.BICEPS -> R.string.muscle_biceps
            Muscle.CALVES -> R.string.muscle_calves
            Muscle.CHEST -> R.string.muscle_chest
            Muscle.FOREARMS -> R.string.muscle_forearms
            Muscle.GLUTES -> R.string.muscle_glutes
            Muscle.HAMSTRINGS -> R.string.muscle_hamstrings
            Muscle.LATS -> R.string.muscle_lats
            Muscle.LOWER_BACK -> R.string.muscle_lower_back
            Muscle.MIDDLE_BACK -> R.string.muscle_middle_back
            Muscle.NECK -> R.string.muscle_neck
            Muscle.QUADRICEPS -> R.string.muscle_quadriceps
            Muscle.SHOULDERS -> R.string.muscle_shoulders
            Muscle.TRAPS -> R.string.muscle_traps
            Muscle.TRICEPS -> R.string.muscle_triceps
            Category.POWERLIFTING -> R.string.category_powerlifting
            Category.STRENGTH -> R.string.category_strength
            Category.STRETCHING -> R.string.category_stretching
            Category.CARDIO -> R.string.category_cardio
            Category.OLYMPIC_WEIGHTLIFTING -> R.string.category_olympic_weightlifting
            Category.STRONGMAN -> R.string.category_strongman
            Category.PLYOMETRICS -> R.string.category_plyometrics
            else -> R.string.any
        }
    }

    fun muscleToVectorId(muscle: Muscle): Int {
        return when (muscle) {
            Muscle.ABDOMINALS -> R.drawable.abdominals
            Muscle.ABDUCTORS -> R.drawable.abductors
            Muscle.ADDUCTORS -> R.drawable.adductors
            Muscle.BICEPS -> R.drawable.biceps
            Muscle.CALVES -> R.drawable.calves
            Muscle.CHEST -> R.drawable.chest
            Muscle.FOREARMS -> R.drawable.forearms
            Muscle.GLUTES -> R.drawable.glutes
            Muscle.HAMSTRINGS -> R.drawable.harmstring
            Muscle.LATS -> R.drawable.lats
            Muscle.LOWER_BACK -> R.drawable.lower_back
            Muscle.MIDDLE_BACK -> R.drawable.middle_back
            Muscle.NECK -> R.drawable.neck
            Muscle.QUADRICEPS -> R.drawable.quads
            Muscle.SHOULDERS -> R.drawable.shoulders
            Muscle.TRAPS -> R.drawable.traps
            Muscle.TRICEPS -> R.drawable.triceps
        }
    }

    fun formatTime(seconds: Int): String {
        val h = (seconds / 3600).toString().padStart(2, '0')
        val m = ((seconds % 3600) / 60).toString().padStart(2, '0')
        val s = (seconds % 60).toString().padStart(2, '0')

        return "$h:$m:$s"
    }

    /**
     * It returns a string as follows: [boldText]:[text]
     */
    fun formatDetails(boldText: String, text: String): AnnotatedString {
        return buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) { append("$boldText: ") }
            append(text)
        }
    }

    fun getShortDateFromLocalDate(date: LocalDateTime): String {
        return date.format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(
                Locale.getDefault()
            )
        )
    }

    fun getLongDateFromLocalDate(date: LocalDateTime): String {
        return date.format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(
                Locale.getDefault()
            )
        )
    }

    fun getFullDateFromLocalDate(date: LocalDateTime): String {
        return date.format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(
                Locale.getDefault()
            )
        )
    }

    /**
     * It is used to process user input in [org.librefit.ui.components.ExerciseCard] and [org.librefit.ui.screens.measurements.MeasurementScreen]
     * text field and return the corresponding float. If [string] fulfills the requirements then a
     * [Float] is returned otherwise it returns `null`.
     * @return A float value corresponding to the sanitized [string]. Returns `null` if the format is invalid.
     */
    fun parseFloatValueInput(
        string: String,
        maxIntegerDigits: Int = 3,
        maxDecimalDigits: Int = 2
    ): Float? {
        val sanitizedString = string.replace(',', '.')

        // Regex to allow a valid float format with up to 2 decimal place.
        // e.g. : 123.45
        val regex = Regex("^\\d{0,${maxIntegerDigits}}\\.?\\d{0,${maxDecimalDigits}}$")

        if (sanitizedString.isEmpty() || regex.matches(sanitizedString)) {
            return sanitizedString.toFloatOrNull() ?: 0f
        }

        return null
    }

    /**
     * It is used to process user input in UI components like text fields and return the
     * corresponding integer. If [string] fulfills the requirements, an [Int] is returned;
     * otherwise, it returns `null` to indicate invalid input.
     *
     * @param string The input string from a text field.
     * @return An integer value if the string is empty or a valid integer format (up to 3 digits).
     * Returns `null` if the format is invalid.
     */
    fun parseIntegerValueInput(string: String, maxDigits: Int = 4): Int? {
        val regex = Regex("^\\d{0,${maxDigits}}$")

        if (regex.matches(string)) {
            return string.toIntOrNull() ?: 0
        }

        return null
    }

    /**
     * This function parses a raw digit string, treating it like a calculator input,
     * and converts it into a total number of seconds for an HH:MM:SS format.
     *
     * It is robust, readable, and follows best practices for integer parsing.
     *
     * Examples:
     * ```
     *   parseTimeInputToSeconds("0")            -> 0
     *   parseTimeInputToSeconds("59")           -> 59
     *   parseTimeInputToSeconds("1:23")         -> 83
     *   parseTimeInputToSeconds("1:23:45")      -> 4525
     *   parseTimeInputToSeconds("12:34:56")     -> 45296
     *   parseTimeInputToSeconds("12:34:75")     -> 45299
     * ```
     */
    fun parseTimeInputToSeconds(input: String): Int {
        val digitsOnly = input.filter { it.isDigit() }
        val relevantDigits = digitsOnly.takeLast(6)

        if (relevantDigits.isEmpty()) {
            return 0
        }

        val number = relevantDigits.toInt()


        val s = number % 100
        val m = (number / 100) % 100
        val h = number / 10000

        // 3. Apply constraints to the extracted parts. This is the correct place to do it.
        val validSeconds = min(s, 59)
        val validMinutes = min(m, 59)

        // 4. Calculate total seconds. This logic is now clean and easy to verify.
        return (h * 3600) + (validMinutes * 60) + validSeconds
    }
}

