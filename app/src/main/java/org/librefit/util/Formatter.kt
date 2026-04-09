/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.util

import androidx.annotation.IntRange
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import org.librefit.R
import org.librefit.enums.SetMode
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import org.librefit.enums.exercise.ExerciseProperty
import org.librefit.enums.exercise.Force
import org.librefit.enums.exercise.Level
import org.librefit.enums.exercise.Mechanic
import org.librefit.enums.exercise.Muscle
import org.librefit.enums.userPreferences.DialogPreference
import org.librefit.enums.userPreferences.Language
import org.librefit.enums.userPreferences.ThemeMode
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
            null -> R.string.any
        }
    }

    fun setModeToStringId(setMode: SetMode): Int {
        return when (setMode) {
            SetMode.LOAD -> R.string.load
            SetMode.BODYWEIGHT_WITH_LOAD -> R.string.bodyweight_with_load
            SetMode.BODYWEIGHT -> R.string.bodyweight
            SetMode.DURATION -> R.string.duration
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

    fun preferenceToStringId(dialogPreference: DialogPreference): Int {
        return when (dialogPreference) {
            Language.ENGLISH -> R.string.language_english_nt
            Language.ITALIAN -> R.string.language_italian_nt
            Language.GERMAN -> R.string.language_german_nt
            Language.DUTCH -> R.string.language_dutch_nt
            Language.SYSTEM -> R.string.follow_system
            ThemeMode.LIGHT -> R.string.theme_light
            ThemeMode.DARK -> R.string.theme_dark
            ThemeMode.SYSTEM -> R.string.follow_system
        }
    }

    fun formatTime(seconds: Int): String {
        val h = (seconds / 3600).toString().padStart(2, '0')
        val m = ((seconds % 3600) / 60).toString().padStart(2, '0')
        val s = (seconds % 60).toString().padStart(2, '0')

        return "$h:$m:$s"
    }

    /**
     * It simply returns the provided [seconds] in minutes and seconds.
     *
     * ```
     * formateSecondsInMinutesAndSeconds(120)  // 02:00
     * formateSecondsInMinutesAndSeconds(20)   // 00:20
     * formateSecondsInMinutesAndSeconds(3601) // 60:01
     * ```
     */
    fun formateSecondsInMinutesAndSeconds(seconds: Int): String {
        val m = (seconds / 60).toString().padStart(2, '0')
        val s = (seconds % 60).toString().padStart(2, '0')
        return "$m:$s"
    }

    /**
     * It returns a string as follows: [boldText]:[text]
     */
    fun formatDetails(boldText: String, text: String): AnnotatedString {
        return buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("$boldText: ") }
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

    fun getDateTimeFromLocalDateTime(date: LocalDateTime): String {
        return date.format(
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(
                Locale.getDefault()
            )
        )
    }

    /**
     * It is used to process user input in [org.librefit.ui.components.ExerciseCard]
     * text field and return the corresponding float.
     * @param string The string value to be processed
     * @return A [Double] value corresponding to the sanitized [string].
     */
    fun parseDoubleFromString(string: String): Double {
        // Keep only digits and dots.
        val sanitized = string
            .replace(",", ".")
            .filter { it.isDigit() || it == '.' }

        // Find the last separator.
        val decimalSeparatorIndex = sanitized.lastIndexOf('.')

        val finalString = if (decimalSeparatorIndex != -1) {
            // Remove all other separators (which are now group separators)
            val integerPart = sanitized
                .take(decimalSeparatorIndex)
                .replace(".", "")
            val fractionalPart = sanitized
                .substring(decimalSeparatorIndex + 1)
                .replace(".", "")

            "$integerPart.$fractionalPart"
        } else {
            // No separators, just a number
            sanitized
        }

        // Safely convert to Double
        return finalString.toDoubleOrNull() ?: 0.0
    }

    /**
     * It is used to process user input in UI components like text fields and return the
     * corresponding number as string.
     *
     * @param string The input string to be converted.
     * @param maxIntegerDigits Takes the first [maxIntegerDigits] from integer part of [string] starting from the left.
     * @param maxFractionalDigits Takes the first [maxFractionalDigits] from fractional part of [string] starting from the left.
     * @throws IllegalArgumentException if [maxIntegerDigits] or [maxFractionalDigits] are not a value
     * between 0 and 8
     */
    fun normalizeNumericString(
        string: String,
        @IntRange(0, 8) maxIntegerDigits: Int = 3,
        @IntRange(0, 8) maxFractionalDigits: Int = 3,
    ): String {
        require(maxIntegerDigits in 0..8 && maxFractionalDigits in 0..8) {
            "maxIntegerDigits and maxFractionalDigits must be between 0 and 8. maxIntegerDigits: $maxIntegerDigits. " +
                    "maxFractionalDigits: $maxFractionalDigits."
        }

        // Keep only digits and dots.
        val sanitized = string
            .replace(",", ".")
            .filter { it.isDigit() || it == '.' }

        // Find the last separator.
        val decimalSeparatorIndex = sanitized.lastIndexOf('.')

        val value = if (decimalSeparatorIndex != -1) {
            // Remove all other separators (which are now group separators)
            val integerPart = sanitized
                .take(decimalSeparatorIndex)
                .replace(".", "")
                .take(maxIntegerDigits)
            val fractionalPart = sanitized
                .substring(decimalSeparatorIndex + 1)
                .replace(".", "")
                .take(maxFractionalDigits)

            "$integerPart.$fractionalPart"
        } else {
            // No separators, just a number
            sanitized.take(maxIntegerDigits)
        }

        return value.takeIf { it != "." && it.isNotBlank() } ?: ""
    }

    /**
     * It is used to process user input in UI components like text fields and return the
     * corresponding integer.
     *
     * @param string The input string to be converted.
     * @param maxValue The maximum value allowed. By default is [Int.MAX_VALUE]. It must be greater than [minValue].
     * @param minValue The minimum value allowed. By default is [Int.MIN_VALUE]. It must be less than [maxValue].
     * @return The converted [string] value as [Int] or 0 if the string is blank.
     * @throws NumberFormatException If the [string] format cannot be converted as [Int],
     * [String.toInt] launches an exception. However, it should not happen as this function process
     * empty string or with only digits.
     * @throws IllegalArgumentException if [minValue] is greater than [maxValue]
     */
    fun parseIntegerFromString(
        string: String,
        maxValue: Int = Int.MAX_VALUE,
        minValue: Int = Int.MIN_VALUE
    ): Int? {
        require(maxValue >= minValue) { "maxValue must be greater or equal than minValue. maxValue: $maxValue. minValue : $minValue" }

        return string
            .filter { it.isDigit() }
            .ifBlank { null }
            ?.toInt()
            ?.coerceIn(minValue, maxValue)
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

