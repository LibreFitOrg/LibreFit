/*
 * Copyright (c) 2025. LibreFit
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

package org.librefit.db.repository

import kotlinx.coroutines.flow.Flow
import org.librefit.db.dao.WorkoutDao
import org.librefit.db.entity.Workout
import org.librefit.db.relations.ExerciseWithSets
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.ui.models.UiWorkoutWithExercisesAndSets
import org.librefit.ui.models.mappers.toUi

/**
 * Repository class for managing workout data.
 *
 * This class serves as a mediator between [WorkoutDao] and the
 * application, providing a clean API for data access.
 *
 * This class is provided by [org.librefit.di.RepositoryModule].
 *
 * @param workoutDao The [WorkoutDao] instance used to access workout data from the database.
 * @property completedWorkouts Refer to [WorkoutDao.getCompletedWorkouts]
 * @property routines Refer to [WorkoutDao.getRoutines]
 *
 */
class WorkoutRepository(private val workoutDao: WorkoutDao) {
    val completedWorkouts = workoutDao.getCompletedWorkouts()

    val routines = workoutDao.getRoutines()


    fun getWorkout(id: Long): Workout {
        return workoutDao.getWorkout(id)
    }

    suspend fun getWorkoutWithExercisesAndSets(workoutID: Long): UiWorkoutWithExercisesAndSets {
        return workoutDao.getWorkoutWithExercisesAndSets(id = workoutID).toUi()
    }

    suspend fun getRoutineFromRoutineID(routineId: Long): Workout {
        return workoutDao.getRoutineFromRoutineID(routineId) ?: Workout()
    }

    suspend fun updateWorkout(workout: Workout) {
        workoutDao.updateWorkout(workout)
    }

    suspend fun deleteWorkout(workout: Workout) {
        workoutDao.deleteWorkout(workout)
    }

    /**
     * Refer to [WorkoutDao.getCompletedWorkoutsWithExercisesAndSets]
     */
    fun getCompletedWorkoutsWithExercisesAndSets(): Flow<List<WorkoutWithExercisesAndSets>> {
        return workoutDao.getCompletedWorkoutsWithExercisesAndSets()
    }


    /**
     * Refer to [WorkoutDao.getExercisesFromWorkout]
     */
    suspend fun getExercisesFromWorkout(workoutId: Long): List<ExerciseWithSets> {
        return workoutDao.getExercisesFromWorkout(workoutId)
    }

    /**
     * Refer to [WorkoutDao.getCompletedWorkoutsWithExercisesAndSetsFromRoutine]
     */
    suspend fun getCompletedWorkoutsWithExercisesAndSetsFromRoutine(routineId: Long): List<WorkoutWithExercisesAndSets> {
        return workoutDao.getCompletedWorkoutsWithExercisesAndSetsFromRoutine(routineId)
    }

    /**
     * Refer to [WorkoutDao.addWorkoutWithExercisesAndSets]
     */
    suspend fun addWorkoutWithExercisesAndSets(
        workoutWithExercisesAndSets: WorkoutWithExercisesAndSets
    ) {
        workoutDao.addWorkoutWithExercisesAndSets(workoutWithExercisesAndSets)
    }

}