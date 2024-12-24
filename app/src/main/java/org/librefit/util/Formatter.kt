/*
 * Copyright (c) 2024. LibreFit
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

import org.librefit.R
import org.librefit.enums.Category
import org.librefit.enums.Equipment
import org.librefit.enums.Force
import org.librefit.enums.Level
import org.librefit.enums.Mechanic
import org.librefit.enums.Muscle
import java.util.Locale

fun exerciseEnumToStringId(enum: Enum<*>?) : Int {
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
        Muscle.MIDDLE_BACK -> R.string.muscle_lower_back
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
        else -> -1
    }
}

fun muscleToVectorId(muscle: Muscle) : Int {
    return when(muscle){
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
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs)
}

