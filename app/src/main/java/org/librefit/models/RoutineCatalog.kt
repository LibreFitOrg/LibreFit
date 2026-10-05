/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.models

import org.librefit.R
import org.librefit.enums.SetMode

/**
 * The catalog of ready-to-use routine templates shipped with the app, shown in the library screen.
 *
 * Templates are plain Kotlin values rather than a parsed JSON file: [RoutineTemplate.titleRes] and
 * [RoutineTemplate.descriptionRes] reference string resources statically, so R8 never strips them
 * and neither runtime resource lookup nor JSON schema validation is involved. Adding a template is
 * a two-step change: declare its strings in `strings.xml`, then add an entry to [all].
 *
 * Templates reference exercises of the dataset by id (`res/raw/exercises.json`), resolved when a
 * template is copied to the user's routines by
 * [org.librefit.db.repository.RoutineTemplateRepository].
 *
 * @see ROUTINES.md for an overview of the catalog.
 */
object RoutineCatalog {

    /**
     * Every template shipped with the app, grouped by [RoutineCategory] in display order.
     */
    val all: List<RoutineTemplate> = listOf(
        // Beginner
        RoutineTemplate(
            id = "Dumbbell_Discovery",
            titleRes = R.string.routine_dumbbell_discovery_title,
            descriptionRes = R.string.routine_dumbbell_discovery_desc,
            category = RoutineCategory.BEGINNER,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Goblet_Squat",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Dumbbell_Bench_Press",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 10,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "One-Arm_Dumbbell_Row",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 10,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Dumbbell_Shoulder_Press",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 10,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Alternate_Hammer_Curl",
                    setMode = SetMode.LOAD,
                    sets = 2,
                    reps = 12
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Dumbbell_One-Arm_Triceps_Extension",
                    setMode = SetMode.LOAD,
                    sets = 2,
                    reps = 12
                )
            )
        ),

        RoutineTemplate(
            id = "Resistance_Bands_Full_Body",
            titleRes = R.string.routine_resistance_bands_full_body_title,
            descriptionRes = R.string.routine_resistance_bands_full_body_desc,
            category = RoutineCategory.BEGINNER,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Squats_-_With_Bands",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 15
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Bench_Press_-_With_Bands",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Back_Flyes_-_With_Bands",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12,
                    restTime = 45
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Lateral_Raise_-_With_Bands",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 15,
                    restTime = 45
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "External_Rotation_with_Band",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 15,
                    restTime = 45
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Calf_Raises_-_With_Bands",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 20,
                    restTime = 30
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Crunches",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 15,
                    restTime = 45
                )
            )
        ),

        // Full Body
        RoutineTemplate(
            id = "Dumbbell_Strength_Full_Body",
            titleRes = R.string.routine_dumbbell_strength_full_body_title,
            descriptionRes = R.string.routine_dumbbell_strength_full_body_desc,
            category = RoutineCategory.FULL_BODY,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Goblet_Squat",
                    setMode = SetMode.LOAD,
                    sets = 4,
                    reps = 8,
                    restTime = 120
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Dumbbell_Bench_Press",
                    setMode = SetMode.LOAD,
                    sets = 4,
                    reps = 8,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "One-Arm_Dumbbell_Row",
                    setMode = SetMode.LOAD,
                    sets = 4,
                    reps = 8,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Dumbbell_Shoulder_Press",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 8,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Dumbbell_Lunges",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 10,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Alternate_Hammer_Curl",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 10
                )
            )
        ),

        RoutineTemplate(
            id = "Full_Body_Beginner_Bodyweight",
            titleRes = R.string.routine_full_body_beginner_bodyweight_title,
            descriptionRes = R.string.routine_full_body_beginner_bodyweight_desc,
            category = RoutineCategory.FULL_BODY,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Bodyweight_Squat",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 12
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Incline_Push-Up",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 10
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Superman",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 12,
                    restTime = 45
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Crunches",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 15,
                    restTime = 45
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Plank",
                    setMode = SetMode.DURATION,
                    sets = 2,
                    elapsedTime = 30,
                    restTime = 45
                )
            )
        ),

        RoutineTemplate(
            id = "Full_Body_Beginner_Machines",
            titleRes = R.string.routine_full_body_beginner_machines_title,
            descriptionRes = R.string.routine_full_body_beginner_machines_desc,
            category = RoutineCategory.FULL_BODY,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Leg_Press",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Wide-Grip_Lat_Pulldown",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Leverage_Chest_Press",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Seated_Leg_Curl",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Ab_Crunch_Machine",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 15,
                    restTime = 45
                )
            )
        ),

        RoutineTemplate(
            id = "Full_Body_Strength_Five_By_Five",
            titleRes = R.string.routine_full_body_strength_five_by_five_full_body_a_title,
            descriptionRes = R.string.routine_full_body_strength_five_by_five_full_body_a_desc,
            category = RoutineCategory.FULL_BODY,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Barbell_Squat",
                    setMode = SetMode.LOAD,
                    sets = 5,
                    reps = 5,
                    restTime = 180
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Barbell_Bench_Press_-_Medium_Grip",
                    setMode = SetMode.LOAD,
                    sets = 5,
                    reps = 5,
                    restTime = 180
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Bent_Over_Barbell_Row",
                    setMode = SetMode.LOAD,
                    sets = 5,
                    reps = 5,
                    restTime = 180
                )
            )
        ),

        RoutineTemplate(
            id = "Full_Body_Strength_Five_By_Five_B",
            titleRes = R.string.routine_full_body_strength_five_by_five_b_title,
            descriptionRes = R.string.routine_full_body_strength_five_by_five_b_desc,
            category = RoutineCategory.FULL_BODY,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Barbell_Deadlift",
                    setMode = SetMode.LOAD,
                    sets = 4,
                    reps = 6,
                    restTime = 180
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Barbell_Shoulder_Press",
                    setMode = SetMode.LOAD,
                    sets = 5,
                    reps = 5,
                    restTime = 150
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Bent_Over_Barbell_Row",
                    setMode = SetMode.LOAD,
                    sets = 5,
                    reps = 5,
                    restTime = 150
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Dips_-_Chest_Version",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 8,
                    restTime = 90
                )
            )
        ),

        RoutineTemplate(
            id = "Home_No_Equipment_Full_Body",
            titleRes = R.string.routine_home_no_equipment_full_body_title,
            descriptionRes = R.string.routine_home_no_equipment_full_body_desc,
            category = RoutineCategory.FULL_BODY,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Bodyweight_Squat",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 15
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Push-Up_Wide",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 12
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Bodyweight_Walking_Lunge",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 12
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Superman",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 15,
                    restTime = 45
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Sit-Up",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 15,
                    restTime = 45
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Plank",
                    setMode = SetMode.DURATION,
                    sets = 3,
                    elapsedTime = 30,
                    restTime = 45
                )
            )
        ),

        // Ppl
        RoutineTemplate(
            id = "PPL_Legs",
            titleRes = R.string.routine_ppl_legs_title,
            descriptionRes = R.string.routine_ppl_legs_desc,
            category = RoutineCategory.PPL,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Barbell_Squat",
                    setMode = SetMode.LOAD,
                    sets = 4,
                    reps = 6,
                    restTime = 180
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Stiff-Legged_Barbell_Deadlift",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 8,
                    restTime = 150
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Leg_Press",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 10,
                    restTime = 120
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Lying_Leg_Curls",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 10
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Standing_Barbell_Calf_Raise",
                    setMode = SetMode.LOAD,
                    sets = 4,
                    reps = 12
                )
            )
        ),

        RoutineTemplate(
            id = "PPL_Pull",
            titleRes = R.string.routine_ppl_pull_title,
            descriptionRes = R.string.routine_ppl_pull_desc,
            category = RoutineCategory.PPL,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Chin-Up",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 4,
                    reps = 6,
                    restTime = 180
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Bent_Over_Barbell_Row",
                    setMode = SetMode.LOAD,
                    sets = 4,
                    reps = 6,
                    restTime = 150
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Wide-Grip_Lat_Pulldown",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 10,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Face_Pull",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "EZ-Bar_Curl",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 10
                )
            )
        ),

        RoutineTemplate(
            id = "PPL_Push",
            titleRes = R.string.routine_ppl_push_title,
            descriptionRes = R.string.routine_ppl_push_desc,
            category = RoutineCategory.PPL,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Barbell_Bench_Press_-_Medium_Grip",
                    setMode = SetMode.LOAD,
                    sets = 4,
                    reps = 6,
                    restTime = 180
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Barbell_Shoulder_Press",
                    setMode = SetMode.LOAD,
                    sets = 4,
                    reps = 6,
                    restTime = 150
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Incline_Dumbbell_Flyes_-_With_A_Twist",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 10,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Dips_-_Triceps_Version",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 8,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Push-Ups_-_Close_Triceps_Position",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 10
                )
            )
        ),

        // Strength
        RoutineTemplate(
            id = "Lower_Body_Strength",
            titleRes = R.string.routine_lower_body_strength_title,
            descriptionRes = R.string.routine_lower_body_strength_desc,
            category = RoutineCategory.STRENGTH,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Barbell_Squat",
                    setMode = SetMode.LOAD,
                    sets = 5,
                    reps = 5,
                    restTime = 180
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Stiff-Legged_Barbell_Deadlift",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 8,
                    restTime = 120
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Leg_Press",
                    setMode = SetMode.LOAD,
                    sets = 4,
                    reps = 10,
                    restTime = 120
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Hyperextensions_Back_Extensions",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 12
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Seated_Calf_Raise",
                    setMode = SetMode.LOAD,
                    sets = 4,
                    reps = 15,
                    restTime = 45
                )
            )
        ),

        RoutineTemplate(
            id = "Upper_Body_Strength",
            titleRes = R.string.routine_upper_body_strength_title,
            descriptionRes = R.string.routine_upper_body_strength_desc,
            category = RoutineCategory.STRENGTH,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Barbell_Bench_Press_-_Medium_Grip",
                    setMode = SetMode.LOAD,
                    sets = 4,
                    reps = 5,
                    restTime = 180
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Bent_Over_Barbell_Row",
                    setMode = SetMode.LOAD,
                    sets = 4,
                    reps = 5,
                    restTime = 150
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Barbell_Shoulder_Press",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 6,
                    restTime = 150
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Pullups",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 8,
                    restTime = 120
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "EZ-Bar_Curl",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 10
                )
            )
        ),

        // Hypertrophy
        RoutineTemplate(
            id = "Arms_And_Shoulders",
            titleRes = R.string.routine_arms_and_shoulders_title,
            descriptionRes = R.string.routine_arms_and_shoulders_desc,
            category = RoutineCategory.HYPERTROPHY,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Arnold_Dumbbell_Press",
                    setMode = SetMode.LOAD,
                    sets = 4,
                    reps = 10,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Side_Laterals_to_Front_Raise",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Face_Pull",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 15
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "EZ-Bar_Curl",
                    setMode = SetMode.LOAD,
                    sets = 4,
                    reps = 10,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Alternate_Hammer_Curl",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Triceps_Pushdown_-_Rope_Attachment",
                    setMode = SetMode.LOAD,
                    sets = 4,
                    reps = 10,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Bench_Dips",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 12
                )
            )
        ),

        RoutineTemplate(
            id = "Chest_And_Back",
            titleRes = R.string.routine_chest_and_back_title,
            descriptionRes = R.string.routine_chest_and_back_desc,
            category = RoutineCategory.HYPERTROPHY,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Barbell_Bench_Press_-_Medium_Grip",
                    setMode = SetMode.LOAD,
                    sets = 4,
                    reps = 8,
                    restTime = 120
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Pullups",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 4,
                    reps = 8,
                    restTime = 120
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Dumbbell_Flyes",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "One-Arm_Dumbbell_Row",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 10,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Straight-Arm_Pulldown",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12
                )
            )
        ),

        RoutineTemplate(
            id = "Lower_Body_Hypertrophy",
            titleRes = R.string.routine_lower_body_hypertrophy_title,
            descriptionRes = R.string.routine_lower_body_hypertrophy_desc,
            category = RoutineCategory.HYPERTROPHY,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Barbell_Squat",
                    setMode = SetMode.LOAD,
                    sets = 4,
                    reps = 8,
                    restTime = 150
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Stiff-Legged_Barbell_Deadlift",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 10,
                    restTime = 120
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Barbell_Lunge",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 10,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Lying_Leg_Curls",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Single-Leg_Leg_Extension",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Seated_Calf_Raise",
                    setMode = SetMode.LOAD,
                    sets = 4,
                    reps = 15,
                    restTime = 45
                )
            )
        ),

        RoutineTemplate(
            id = "Upper_Body_Hypertrophy",
            titleRes = R.string.routine_upper_body_hypertrophy_title,
            descriptionRes = R.string.routine_upper_body_hypertrophy_desc,
            category = RoutineCategory.HYPERTROPHY,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Barbell_Incline_Bench_Press_-_Medium_Grip",
                    setMode = SetMode.LOAD,
                    sets = 4,
                    reps = 8,
                    restTime = 120
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Seated_Cable_Rows",
                    setMode = SetMode.LOAD,
                    sets = 4,
                    reps = 8,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Dumbbell_Shoulder_Press",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 10,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Wide-Grip_Lat_Pulldown",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 10,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Dumbbell_Flyes",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "EZ-Bar_Curl",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Cable_Rope_Overhead_Triceps_Extension",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12
                )
            )
        ),

        // Bodyweight
        RoutineTemplate(
            id = "Bodyweight_Legs_Home",
            titleRes = R.string.routine_bodyweight_legs_home_title,
            descriptionRes = R.string.routine_bodyweight_legs_home_desc,
            category = RoutineCategory.BODYWEIGHT,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Bodyweight_Squat",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 4,
                    reps = 20
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Butt_Lift_Bridge",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 15,
                    restTime = 45
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Bodyweight_Walking_Lunge",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 16
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Freehand_Jump_Squat",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 15
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Pistol_Squat",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 6,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Rocking_Standing_Calf_Raise",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 20,
                    restTime = 30
                )
            )
        ),

        RoutineTemplate(
            id = "Bodyweight_Upper_Home",
            titleRes = R.string.routine_bodyweight_upper_home_title,
            descriptionRes = R.string.routine_bodyweight_upper_home_desc,
            category = RoutineCategory.BODYWEIGHT,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Decline_Push-Up",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 4,
                    reps = 10,
                    restTime = 75
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Push-Up_Wide",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 15
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Pullups",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 4,
                    reps = 8,
                    restTime = 120
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Bench_Dips",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 12
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Plank_Shoulder_Taps",
                    setMode = SetMode.DURATION,
                    sets = 3,
                    elapsedTime = 40,
                    restTime = 45
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Superman",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 15,
                    restTime = 45
                )
            )
        ),

        RoutineTemplate(
            id = "Core_And_Abs",
            titleRes = R.string.routine_core_and_abs_title,
            descriptionRes = R.string.routine_core_and_abs_desc,
            category = RoutineCategory.BODYWEIGHT,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Plank",
                    setMode = SetMode.DURATION,
                    sets = 3,
                    elapsedTime = 45,
                    restTime = 45
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Hanging_Leg_Raise",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 12
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Russian_Twist",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 20,
                    restTime = 45
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Crunches",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 15,
                    restTime = 45
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Dead_Bug",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 12,
                    restTime = 45
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Flutter_Kicks",
                    setMode = SetMode.DURATION,
                    sets = 3,
                    elapsedTime = 30,
                    restTime = 45
                )
            )
        ),

        // Cardio
        RoutineTemplate(
            id = "Cardio_Burn_Machines",
            titleRes = R.string.routine_cardio_burn_machines_title,
            descriptionRes = R.string.routine_cardio_burn_machines_desc,
            category = RoutineCategory.CARDIO,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Jogging_Treadmill",
                    setMode = SetMode.DURATION,
                    sets = 1,
                    elapsedTime = 1200
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Bicycling_Stationary",
                    setMode = SetMode.DURATION,
                    sets = 1,
                    elapsedTime = 900
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Elliptical_Trainer",
                    setMode = SetMode.DURATION,
                    sets = 1,
                    elapsedTime = 600
                )
            )
        ),

        RoutineTemplate(
            id = "Metabolic_Dumbbell_Circuit",
            titleRes = R.string.routine_metabolic_dumbbell_circuit_title,
            descriptionRes = R.string.routine_metabolic_dumbbell_circuit_desc,
            category = RoutineCategory.CARDIO,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Goblet_Squat",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 15,
                    restTime = 45
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "One-Arm_Kettlebell_Swings",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12,
                    restTime = 45
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Dumbbell_Shoulder_Press",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12,
                    restTime = 45
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Dumbbell_Lunges",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12,
                    restTime = 45
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "One-Arm_Dumbbell_Row",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12,
                    restTime = 45
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Mountain_Climbers",
                    setMode = SetMode.DURATION,
                    sets = 3,
                    elapsedTime = 30,
                    restTime = 30
                )
            )
        ),

        RoutineTemplate(
            id = "Treadmill_Interval_Run",
            titleRes = R.string.routine_treadmill_interval_run_title,
            descriptionRes = R.string.routine_treadmill_interval_run_desc,
            category = RoutineCategory.CARDIO,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Jogging_Treadmill",
                    setMode = SetMode.DURATION,
                    sets = 1,
                    elapsedTime = 300,
                    restTime = 0
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Running_Treadmill",
                    setMode = SetMode.DURATION,
                    sets = 8,
                    elapsedTime = 60,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Walking_Treadmill",
                    setMode = SetMode.DURATION,
                    sets = 1,
                    elapsedTime = 300,
                    restTime = 0
                )
            )
        ),

        RoutineTemplate(
            id = "Twenty_Minute_HIIT",
            titleRes = R.string.routine_twenty_minute_hiit_title,
            descriptionRes = R.string.routine_twenty_minute_hiit_desc,
            category = RoutineCategory.CARDIO,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Jumping_Jacks",
                    setMode = SetMode.DURATION,
                    sets = 3,
                    elapsedTime = 60,
                    restTime = 30
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Mountain_Climbers",
                    setMode = SetMode.DURATION,
                    sets = 3,
                    elapsedTime = 40,
                    restTime = 30
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Freehand_Jump_Squat",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 15,
                    restTime = 30
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Knee_Tuck_Jump",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 12,
                    restTime = 30
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Rope_Jumping",
                    setMode = SetMode.DURATION,
                    sets = 3,
                    elapsedTime = 60,
                    restTime = 30
                )
            )
        ),

        RoutineTemplate(
            id = "Walk_And_Move",
            titleRes = R.string.routine_walk_and_move_title,
            descriptionRes = R.string.routine_walk_and_move_desc,
            category = RoutineCategory.CARDIO,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Walking_Treadmill",
                    setMode = SetMode.DURATION,
                    sets = 1,
                    elapsedTime = 1800,
                    restTime = 0
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Stairmaster",
                    setMode = SetMode.DURATION,
                    sets = 1,
                    elapsedTime = 600
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Jumping_Jacks",
                    setMode = SetMode.DURATION,
                    sets = 3,
                    elapsedTime = 45,
                    restTime = 30
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Rope_Jumping",
                    setMode = SetMode.DURATION,
                    sets = 3,
                    elapsedTime = 60,
                    restTime = 30
                )
            )
        ),

        // Targeted
        RoutineTemplate(
            id = "Glutes_And_Legs",
            titleRes = R.string.routine_glutes_and_legs_title,
            descriptionRes = R.string.routine_glutes_and_legs_desc,
            category = RoutineCategory.TARGETED,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Barbell_Hip_Thrust",
                    setMode = SetMode.LOAD,
                    sets = 4,
                    reps = 10,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Hack_Squat",
                    setMode = SetMode.LOAD,
                    sets = 4,
                    reps = 10,
                    restTime = 120
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Stiff-Legged_Barbell_Deadlift",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 10,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Glute_Kickback",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Dumbbell_Lunges",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Standing_Barbell_Calf_Raise",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 15,
                    restTime = 45
                )
            )
        ),

        RoutineTemplate(
            id = "Posture_And_Upper_Back",
            titleRes = R.string.routine_posture_and_upper_back_title,
            descriptionRes = R.string.routine_posture_and_upper_back_desc,
            category = RoutineCategory.TARGETED,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Face_Pull",
                    setMode = SetMode.LOAD,
                    sets = 4,
                    reps = 15
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Cable_Rope_Rear-Delt_Rows",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12,
                    restTime = 75
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Seated_Bent-Over_Rear_Delt_Raise",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Barbell_Shrug",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12,
                    restTime = 75
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Straight-Arm_Pulldown",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Superman",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 15,
                    restTime = 45
                )
            )
        ),

        RoutineTemplate(
            id = "Wide_Back_Lats",
            titleRes = R.string.routine_wide_back_lats_title,
            descriptionRes = R.string.routine_wide_back_lats_desc,
            category = RoutineCategory.TARGETED,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Pullups",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 4,
                    reps = 8,
                    restTime = 120
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Wide-Grip_Lat_Pulldown",
                    setMode = SetMode.LOAD,
                    sets = 4,
                    reps = 10,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Seated_Cable_Rows",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 10,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Straight-Arm_Pulldown",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 12
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Face_Pull",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 15
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Hyperextensions_Back_Extensions",
                    setMode = SetMode.BODYWEIGHT,
                    sets = 3,
                    reps = 12
                )
            )
        ),

        // Mobility
        RoutineTemplate(
            id = "Comeback_After_A_Break",
            titleRes = R.string.routine_comeback_after_a_break_title,
            descriptionRes = R.string.routine_comeback_after_a_break_desc,
            category = RoutineCategory.MOBILITY,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Leg_Press",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 10,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Leverage_Chest_Press",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 10,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Wide-Grip_Lat_Pulldown",
                    setMode = SetMode.LOAD,
                    sets = 3,
                    reps = 10,
                    restTime = 90
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Seated_Leg_Curl",
                    setMode = SetMode.LOAD,
                    sets = 2,
                    reps = 12
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Cable_Seated_Lateral_Raise",
                    setMode = SetMode.LOAD,
                    sets = 2,
                    reps = 12
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Plank",
                    setMode = SetMode.DURATION,
                    sets = 2,
                    elapsedTime = 30
                )
            )
        ),

        RoutineTemplate(
            id = "Mobility_And_Stretching",
            titleRes = R.string.routine_mobility_and_stretching_title,
            descriptionRes = R.string.routine_mobility_and_stretching_desc,
            category = RoutineCategory.MOBILITY,
            exercises = listOf(
                RoutineTemplateExercise(
                    idExerciseDC = "Cat_Stretch",
                    setMode = SetMode.DURATION,
                    sets = 2,
                    elapsedTime = 30,
                    restTime = 30
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Cobra_Stretch",
                    setMode = SetMode.DURATION,
                    sets = 2,
                    elapsedTime = 30,
                    restTime = 30
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Behind_Head_Chest_Stretch",
                    setMode = SetMode.DURATION,
                    sets = 2,
                    elapsedTime = 30,
                    restTime = 30
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Chair_Lower_Back_Stretch",
                    setMode = SetMode.DURATION,
                    sets = 2,
                    elapsedTime = 30,
                    restTime = 30
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Chair_Leg_Extended_Stretch",
                    setMode = SetMode.DURATION,
                    sets = 2,
                    elapsedTime = 30,
                    restTime = 30
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Calf_Stretch_Hands_Against_Wall",
                    setMode = SetMode.DURATION,
                    sets = 2,
                    elapsedTime = 30,
                    restTime = 30
                ),
                RoutineTemplateExercise(
                    idExerciseDC = "Chin_To_Chest_Stretch",
                    setMode = SetMode.DURATION,
                    sets = 2,
                    elapsedTime = 30,
                    restTime = 30
                )
            )
        )
    )

    /**
     * Returns the templates of the passed [category], in display order.
     */
    fun ofCategory(category: RoutineCategory): List<RoutineTemplate> =
        all.filter { it.category == category }

    /**
     * Returns the template with the passed [id], or null when it is not part of the catalog.
     */
    fun findById(id: String): RoutineTemplate? = all.find { it.id == id }
}
