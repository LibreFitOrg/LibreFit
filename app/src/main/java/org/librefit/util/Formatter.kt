package org.librefit.util

import org.librefit.R
import org.librefit.data.Category
import org.librefit.data.Equipment
import org.librefit.data.Force
import org.librefit.data.Level
import org.librefit.data.Mechanic
import org.librefit.data.Muscle

fun exerciseEnumToStringId(enum : Enum<*>) : Int {
    return when (enum) {
        Force.PUSH -> R.string.label_force_push
        Force.PULL -> R.string.label_force_pull
        Force.STATIC -> R.string.label_force_static
        Level.BEGINNER -> R.string.label_level_beginner
        Level.INTERMEDIATE -> R.string.label_level_intermediate
        Level.EXPERT -> R.string.label_level_expert
        Mechanic.ISOLATION -> R.string.label_mechanic_isolation
        Mechanic.COMPOUND -> R.string.label_mechanic_compound
        Equipment.MEDICINE_BALL -> R.string.label_equipment_medicine_ball
        Equipment.DUMBBELL -> R.string.label_equipment_dumbbell
        Equipment.BODY_ONLY -> R.string.label_equipment_body_only
        Equipment.BANDS -> R.string.label_equipment_bands
        Equipment.KETTLEBELLS -> R.string.label_equipment_kettlebells
        Equipment.FOAM_ROLL -> R.string.label_equipment_foam_roll
        Equipment.CABLE -> R.string.label_equipment_cable
        Equipment.MACHINE -> R.string.label_equipment_machine
        Equipment.BARBELL -> R.string.label_equipment_barbell
        Equipment.EXERCISE_BALL -> R.string.label_equipment_exercise_ball
        Equipment.E_Z_CURL_BAR -> R.string.label_equipment_ez_curl_bar
        Equipment.OTHER -> R.string.label_equipment_other
        Muscle.ABDOMINALS -> R.string.label_muscle_abdominals
        Muscle.ABDUCTORS -> R.string.label_muscle_abductors
        Muscle.ADDUCTORS -> R.string.label_muscle_adductors
        Muscle.BICEPS -> R.string.label_muscle_biceps
        Muscle.CALVES -> R.string.label_muscle_calves
        Muscle.CHEST -> R.string.label_muscle_chest
        Muscle.FOREARMS -> R.string.label_muscle_forearms
        Muscle.GLUTES -> R.string.label_muscle_glutes
        Muscle.HAMSTRINGS -> R.string.label_muscle_hamstrings
        Muscle.LATS -> R.string.label_muscle_lats
        Muscle.LOWER_BACK -> R.string.label_muscle_lower_back
        Muscle.MIDDLE_BACK -> R.string.label_muscle_lower_back
        Muscle.NECK -> R.string.label_muscle_neck
        Muscle.QUADRICEPS -> R.string.label_muscle_quadriceps
        Muscle.SHOULDERS -> R.string.label_muscle_shoulders
        Muscle.TRAPS -> R.string.label_muscle_traps
        Muscle.TRICEPS -> R.string.label_muscle_triceps
        Category.POWERLIFTING -> R.string.label_category_powerlifting
        Category.STRENGTH -> R.string.label_category_strength
        Category.STRETCHING -> R.string.label_category_stretching
        Category.CARDIO -> R.string.label_category_cardio
        Category.OLYMPIC_WEIGHTLIFTING -> R.string.label_category_olympic_weightlifting
        Category.STRONGMAN -> R.string.label_category_strongman
        Category.PLYOMETRICS -> R.string.label_category_plyometrics
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



