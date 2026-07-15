/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.enums

enum class WorkoutServiceActions(val string: String) {
    START_STOPWATCH("START_STOPWATCH"),
    PAUSE_STOPWATCH("PAUSE_STOPWATCH"),
    START_REST_TIMER("START_REST_TIMER"),
    WORKOUT_FOCUS("WORKOUT_FOCUS"),
    STOP_SERVICE("STOP_SERVICE"),
    SET_ELAPSED_TIME("SET_ELAPSED_TIME"),
    ADD_TEN_SECONDS_TO_REST_TIMER("ADD_TEN_SECONDS_TO_REST_TIMER"),
    SUBTRACT_TEN_SECONDS_TO_REST_TIMER("SUBTRACT_TEN_SECONDS_TO_REST_TIMER")
}