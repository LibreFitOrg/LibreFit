/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.enums.healthConnect

import androidx.health.connect.client.records.ExerciseSegment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The Health Connect exercise segment type assigned to a LibreFit exercise.
 * Names are stored instead of Android integer constants so persisted mappings stay stable.
 */
@Serializable
enum class HealthConnectSegmentType(val exerciseSegmentType: Int) {
    @SerialName("arm_curl")
    ARM_CURL(ExerciseSegment.EXERCISE_SEGMENT_TYPE_ARM_CURL),

    @SerialName("back_extension")
    BACK_EXTENSION(ExerciseSegment.EXERCISE_SEGMENT_TYPE_BACK_EXTENSION),

    @SerialName("ball_slam")
    BALL_SLAM(ExerciseSegment.EXERCISE_SEGMENT_TYPE_BALL_SLAM),

    @SerialName("barbell_shoulder_press")
    BARBELL_SHOULDER_PRESS(ExerciseSegment.EXERCISE_SEGMENT_TYPE_BARBELL_SHOULDER_PRESS),

    @SerialName("bench_press")
    BENCH_PRESS(ExerciseSegment.EXERCISE_SEGMENT_TYPE_BENCH_PRESS),

    @SerialName("bench_sit_up")
    BENCH_SIT_UP(ExerciseSegment.EXERCISE_SEGMENT_TYPE_BENCH_SIT_UP),

    @SerialName("biking")
    BIKING(ExerciseSegment.EXERCISE_SEGMENT_TYPE_BIKING),

    @SerialName("biking_stationary")
    BIKING_STATIONARY(ExerciseSegment.EXERCISE_SEGMENT_TYPE_BIKING_STATIONARY),

    @SerialName("burpee")
    BURPEE(ExerciseSegment.EXERCISE_SEGMENT_TYPE_BURPEE),

    @SerialName("crunch")
    CRUNCH(ExerciseSegment.EXERCISE_SEGMENT_TYPE_CRUNCH),

    @SerialName("deadlift")
    DEADLIFT(ExerciseSegment.EXERCISE_SEGMENT_TYPE_DEADLIFT),

    @SerialName("double_arm_triceps_extension")
    DOUBLE_ARM_TRICEPS_EXTENSION(
        ExerciseSegment.EXERCISE_SEGMENT_TYPE_DOUBLE_ARM_TRICEPS_EXTENSION
    ),

    @SerialName("dumbbell_curl_left_arm")
    DUMBBELL_CURL_LEFT_ARM(ExerciseSegment.EXERCISE_SEGMENT_TYPE_DUMBBELL_CURL_LEFT_ARM),

    @SerialName("dumbbell_curl_right_arm")
    DUMBBELL_CURL_RIGHT_ARM(ExerciseSegment.EXERCISE_SEGMENT_TYPE_DUMBBELL_CURL_RIGHT_ARM),

    @SerialName("dumbbell_front_raise")
    DUMBBELL_FRONT_RAISE(ExerciseSegment.EXERCISE_SEGMENT_TYPE_DUMBBELL_FRONT_RAISE),

    @SerialName("dumbbell_lateral_raise")
    DUMBBELL_LATERAL_RAISE(ExerciseSegment.EXERCISE_SEGMENT_TYPE_DUMBBELL_LATERAL_RAISE),

    @SerialName("dumbbell_row")
    DUMBBELL_ROW(ExerciseSegment.EXERCISE_SEGMENT_TYPE_DUMBBELL_ROW),

    @SerialName("dumbbell_triceps_extension_left_arm")
    DUMBBELL_TRICEPS_EXTENSION_LEFT_ARM(
        ExerciseSegment.EXERCISE_SEGMENT_TYPE_DUMBBELL_TRICEPS_EXTENSION_LEFT_ARM
    ),

    @SerialName("dumbbell_triceps_extension_right_arm")
    DUMBBELL_TRICEPS_EXTENSION_RIGHT_ARM(
        ExerciseSegment.EXERCISE_SEGMENT_TYPE_DUMBBELL_TRICEPS_EXTENSION_RIGHT_ARM
    ),

    @SerialName("dumbbell_triceps_extension_two_arm")
    DUMBBELL_TRICEPS_EXTENSION_TWO_ARM(
        ExerciseSegment.EXERCISE_SEGMENT_TYPE_DUMBBELL_TRICEPS_EXTENSION_TWO_ARM
    ),

    @SerialName("elliptical")
    ELLIPTICAL(ExerciseSegment.EXERCISE_SEGMENT_TYPE_ELLIPTICAL),

    @SerialName("forward_twist")
    FORWARD_TWIST(ExerciseSegment.EXERCISE_SEGMENT_TYPE_FORWARD_TWIST),

    @SerialName("front_raise")
    FRONT_RAISE(ExerciseSegment.EXERCISE_SEGMENT_TYPE_FRONT_RAISE),

    @SerialName("high_intensity_interval_training")
    HIGH_INTENSITY_INTERVAL_TRAINING(
        ExerciseSegment.EXERCISE_SEGMENT_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING
    ),

    @SerialName("hip_thrust")
    HIP_THRUST(ExerciseSegment.EXERCISE_SEGMENT_TYPE_HIP_THRUST),

    @SerialName("jump_rope")
    JUMP_ROPE(ExerciseSegment.EXERCISE_SEGMENT_TYPE_JUMP_ROPE),

    @SerialName("jumping_jack")
    JUMPING_JACK(ExerciseSegment.EXERCISE_SEGMENT_TYPE_JUMPING_JACK),

    @SerialName("kettlebell_swing")
    KETTLEBELL_SWING(ExerciseSegment.EXERCISE_SEGMENT_TYPE_KETTLEBELL_SWING),

    @SerialName("lateral_raise")
    LATERAL_RAISE(ExerciseSegment.EXERCISE_SEGMENT_TYPE_LATERAL_RAISE),

    @SerialName("lat_pull_down")
    LAT_PULL_DOWN(ExerciseSegment.EXERCISE_SEGMENT_TYPE_LAT_PULL_DOWN),

    @SerialName("leg_curl")
    LEG_CURL(ExerciseSegment.EXERCISE_SEGMENT_TYPE_LEG_CURL),

    @SerialName("leg_extension")
    LEG_EXTENSION(ExerciseSegment.EXERCISE_SEGMENT_TYPE_LEG_EXTENSION),

    @SerialName("leg_press")
    LEG_PRESS(ExerciseSegment.EXERCISE_SEGMENT_TYPE_LEG_PRESS),

    @SerialName("leg_raise")
    LEG_RAISE(ExerciseSegment.EXERCISE_SEGMENT_TYPE_LEG_RAISE),

    @SerialName("lunge")
    LUNGE(ExerciseSegment.EXERCISE_SEGMENT_TYPE_LUNGE),

    @SerialName("mountain_climber")
    MOUNTAIN_CLIMBER(ExerciseSegment.EXERCISE_SEGMENT_TYPE_MOUNTAIN_CLIMBER),

    @SerialName("other_workout")
    OTHER_WORKOUT(ExerciseSegment.EXERCISE_SEGMENT_TYPE_OTHER_WORKOUT),

    @SerialName("plank")
    PLANK(ExerciseSegment.EXERCISE_SEGMENT_TYPE_PLANK),

    @SerialName("pull_up")
    PULL_UP(ExerciseSegment.EXERCISE_SEGMENT_TYPE_PULL_UP),

    @SerialName("punch")
    PUNCH(ExerciseSegment.EXERCISE_SEGMENT_TYPE_PUNCH),

    @SerialName("rowing_machine")
    ROWING_MACHINE(ExerciseSegment.EXERCISE_SEGMENT_TYPE_ROWING_MACHINE),

    @SerialName("running")
    RUNNING(ExerciseSegment.EXERCISE_SEGMENT_TYPE_RUNNING),

    @SerialName("running_treadmill")
    RUNNING_TREADMILL(ExerciseSegment.EXERCISE_SEGMENT_TYPE_RUNNING_TREADMILL),

    @SerialName("shoulder_press")
    SHOULDER_PRESS(ExerciseSegment.EXERCISE_SEGMENT_TYPE_SHOULDER_PRESS),

    @SerialName("single_arm_triceps_extension")
    SINGLE_ARM_TRICEPS_EXTENSION(
        ExerciseSegment.EXERCISE_SEGMENT_TYPE_SINGLE_ARM_TRICEPS_EXTENSION
    ),

    @SerialName("sit_up")
    SIT_UP(ExerciseSegment.EXERCISE_SEGMENT_TYPE_SIT_UP),

    @SerialName("squat")
    SQUAT(ExerciseSegment.EXERCISE_SEGMENT_TYPE_SQUAT),

    @SerialName("stair_climbing")
    STAIR_CLIMBING(ExerciseSegment.EXERCISE_SEGMENT_TYPE_STAIR_CLIMBING),

    @SerialName("stair_climbing_machine")
    STAIR_CLIMBING_MACHINE(ExerciseSegment.EXERCISE_SEGMENT_TYPE_STAIR_CLIMBING_MACHINE),

    @SerialName("stretching")
    STRETCHING(ExerciseSegment.EXERCISE_SEGMENT_TYPE_STRETCHING),

    @SerialName("upper_twist")
    UPPER_TWIST(ExerciseSegment.EXERCISE_SEGMENT_TYPE_UPPER_TWIST),

    @SerialName("walking")
    WALKING(ExerciseSegment.EXERCISE_SEGMENT_TYPE_WALKING),

    @SerialName("weightlifting")
    WEIGHTLIFTING(ExerciseSegment.EXERCISE_SEGMENT_TYPE_WEIGHTLIFTING)
}
