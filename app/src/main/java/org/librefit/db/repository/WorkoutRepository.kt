/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.db.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.librefit.db.dao.WorkoutDao
import org.librefit.db.entity.Workout
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.enums.WorkoutState
import org.librefit.ui.models.UiWorkoutWithExercisesAndSets
import org.librefit.ui.models.mappers.toUi
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository class for managing workout data.
 *
 * This class serves as a mediator between [WorkoutDao] and the
 * application, providing a clean API for data access.
 *
 * @param workoutDao The [WorkoutDao] instance used to access workout data from the database.
 * @property completedWorkouts Refer to [WorkoutDao.getWorkoutsByStateAndOrderedByCompleted]
 * @property routines Refer to [WorkoutDao.getWorkoutsByState]
 * @property completedWorkoutsWithExercisesAndSets Refer to [WorkoutDao.getWorkoutsWithExercisesAndSetsByStateAndOrderedByCompleted]
 *
 */
@Singleton
class WorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao
) {
    private fun WorkoutWithExercisesAndSets.sortedByExercisePosition(): WorkoutWithExercisesAndSets {
        return copy(exercisesWithSets = exercisesWithSets.sortedBy { it.exercise.position })
    }

    val completedWorkouts =
        workoutDao.getWorkoutsByStateAndOrderedByCompleted(WorkoutState.COMPLETED)

    val routines = workoutDao.getWorkoutsByState(WorkoutState.ROUTINE)

    val completedWorkoutsWithExercisesAndSets =
        workoutDao
            .getWorkoutsWithExercisesAndSetsByStateAndOrderedByCompleted(WorkoutState.COMPLETED)
            .map { workouts -> workouts.map { it.sortedByExercisePosition() } }

    val runningWorkoutsWithExercisesAndSets =
        workoutDao.getWorkoutsWithExercisesAndSetsByState(WorkoutState.RUNNING)
            .map { workouts -> workouts.map { it.sortedByExercisePosition() } }



    suspend fun getWorkoutWithExercisesAndSets(workoutID: Long): UiWorkoutWithExercisesAndSets {
        return workoutDao.getWorkoutWithExercisesAndSets(id = workoutID).toUi()
    }

    suspend fun getRoutineFromRoutineID(routineId: Long): Workout {
        return workoutDao.getWorkoutFromRoutineIDAndState(routineId, state = WorkoutState.ROUTINE)
            ?: Workout()
    }

    suspend fun updateWorkout(workout: Workout) {
        workoutDao.updateWorkout(workout)
    }

    suspend fun deleteWorkout(workout: Workout) {
        workoutDao.deleteWorkout(workout)
    }





    /**
     * Refer to [WorkoutDao.getWorkoutsWithExercisesAndSetsFromRoutineByState]
     */
    suspend fun getCompletedWorkoutsWithExercisesAndSetsFromRoutine(routineId: Long): List<WorkoutWithExercisesAndSets> {
        return workoutDao.getWorkoutsWithExercisesAndSetsFromRoutineByState(
            routineId,
            state = WorkoutState.COMPLETED
        ).map { it.sortedByExercisePosition() }
    }

    /**
     * Refer to [WorkoutDao.addWorkoutWithExercisesAndSets]
     */
    suspend fun addWorkoutWithExercisesAndSets(
        workoutWithExercisesAndSets: WorkoutWithExercisesAndSets
    ): Long {
        return workoutDao.addWorkoutWithExercisesAndSets(workoutWithExercisesAndSets)
    }


    fun getCompletedWorkoutsWithExercisesWithIdExerciseDC(idExerciseDC: String): Flow<List<WorkoutWithExercisesAndSets>> {
        return workoutDao
            .getWorkoutsFromIdExerciseDC(idExerciseDC, WorkoutState.COMPLETED)
            .map { list ->
                list.map { w ->
                    w.copy(
                        exercisesWithSets = w.exercisesWithSets
                            .sortedBy { it.exercise.position }
                            .filter { it.exerciseDC.id == idExerciseDC }
                    )
                }
            }
    }
}
