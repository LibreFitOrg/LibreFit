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

package org.librefit.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.librefit.db.entity.Exercise
import org.librefit.db.entity.Set
import org.librefit.db.entity.Workout
import org.librefit.db.relations.ExerciseWithSets
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import java.time.LocalDateTime

@Dao
interface WorkoutDao {
    /**
     * Returns a flow that emits a stream of [org.librefit.db.entity.Workout]s which are routines ([org.librefit.db.entity.Workout.routine] = `true`)
     */
    @Query("SELECT * FROM workouts WHERE 1 = routine ORDER BY title")
    fun getRoutines(): Flow<List<Workout>>

    /**
     * Returns a flow that emits a stream of [Workout]s which are
     * - completed ([Workout.completed] = `true`)
     * - not routines ([Workout.routine] = `false`)
     * - ordered by date from newest to latest ([Workout.completed])
     */
    @Query("SELECT * FROM workouts WHERE 0 = routine ORDER BY completed DESC")
    fun getCompletedWorkouts(): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE id = :id")
    fun getWorkout(id: Long): Workout

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkoutWithExercisesAndSets(id: Long): WorkoutWithExercisesAndSets

    @Query("SELECT * FROM workouts WHERE routine AND routineId = :routineId")
    suspend fun getRoutineFromRoutineID(routineId: Long): Workout?

    @Insert
    suspend fun addWorkout(workout: Workout): Long

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)

    @Insert
    suspend fun addExercise(exercise: Exercise): Long

    @Update
    suspend fun updateExercise(exercise: Exercise)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Insert
    suspend fun addSet(set: Set)

    @Update
    suspend fun updateSet(set: Set)

    @Delete
    suspend fun deleteSet(set: Set)

    /**
     * Returns a flow list of completed [org.librefit.db.relations.WorkoutWithExercisesAndSets]s which are not routines so
     * those which have [Workout.routine] = `false`.
     */
    @Transaction
    @Query("SELECT * FROM workouts WHERE routine = 0 ORDER BY completed DESC")
    fun getCompletedWorkoutsWithExercisesAndSets(): Flow<List<WorkoutWithExercisesAndSets>>


    /**
     * Retrieves the list of [org.librefit.db.relations.ExerciseWithSets] associated with a specific workout.
     * This function queries the database to fetch all exercises that belong to the given [Exercise.workoutId]
     */
    @Transaction
    @Query("SELECT * FROM exercises WHERE workoutId = :workoutId")
    suspend fun getExercisesFromWorkout(workoutId: Long): List<ExerciseWithSets>

    /**
     * Retrieves the list of [Set]s associated with a specific exercise.
     * This function queries the database to fetch all sets that belong to the given [Set.exerciseId]
     */
    @Query("SELECT * FROM sets WHERE exerciseId = :exerciseId")
    suspend fun getSetsFromExercise(exerciseId: Long): List<Set>

    /**
     * Retrieves the list of [WorkoutWithExercisesAndSets]s associated with a specific routine.
     * This function queries the database to fetch all workouts that belong to the given [Workout.routineId]
     */
    @Transaction
    @Query("SELECT * FROM workouts WHERE routineId = :routineId AND routine = 0 ORDER BY completed DESC")
    suspend fun getCompletedWorkoutsWithExercisesAndSetsFromRoutine(routineId: Long): List<WorkoutWithExercisesAndSets>

    /**
     * Adds a workout along with its associated exercises and sets to the database.
     *
     * A [WorkoutWithExercisesAndSets] is considered "new" if its [Workout.id] is 0. In this case, it will be saved as a new entry.
     * If the workout is not new, it will be updated instead.
     *
     * The function performs the following steps:
     * 1. If the workout is new, it saves the workout with the current timestamp if not it updates the existing workout.
     * 3. It retrieves all exercises associated with the workout from the database.
     * 4. It deletes any exercises that are in the database but not in the provided [ExerciseWithSets] list.
     * 5. For each exercise in [workoutWithExercisesAndSets]:
     *    - If the exercise is new (not found in the old exercises), it adds the exercise to the database.
     *    - If the exercise already exists, it updates the existing exercise.
     * 6. It retrieves all sets associated with each exercise.
     * 7. It deletes any sets that are in the database but not in the provided sets.
     * 8. For each set in the provided sets ([ExerciseWithSets.sets]):
     *    - If the set already exists, it updates the set.
     *    - If the set is new, it adds the set to the database.
     *
     * @param workoutWithExercisesAndSets The workout to be added or updated with its exercises and sets
     */
    @Transaction
    suspend fun addWorkoutWithExercisesAndSets(
        workoutWithExercisesAndSets: WorkoutWithExercisesAndSets
    ) {
        val workout = workoutWithExercisesAndSets.workout

        val workoutId = if (workout.id == 0L) {
            if (workout.routine) {
                addWorkout(workout.copy(completed = LocalDateTime.now()))
            } else {
                addWorkout(workout.copy(created = LocalDateTime.now()))
            }
        } else {
            updateWorkout(workout)
            workout.id
        }

        val exercisesWithSets = workoutWithExercisesAndSets.exercisesWithSets
        val oldExercises = getExercisesFromWorkout(workoutId)

        // Create maps for fast O(1) lookups by exercise ID.
        val oldExercisesMap = oldExercises.associateBy { it.exercise.id }
        val newExercisesMap = exercisesWithSets.associateBy { it.exercise.id }

        // Deletes from db the exercises not found in the passed exercises
        val idsToDelete = oldExercisesMap.keys - newExercisesMap.keys
        idsToDelete.forEach { idToDelete ->
            deleteExercise(oldExercisesMap.getValue(idToDelete).exercise)
        }

        exercisesWithSets.forEach { exerciseWithSets ->
            // Check if this exercise existed before.
            val exerciseId = if (exerciseWithSets.exercise.id in oldExercisesMap) {
                updateExercise(exerciseWithSets.exercise)
                exerciseWithSets.exercise.id
            } else {
                addExercise(exerciseWithSets.exercise.copy(id = 0, workoutId = workoutId))
            }

            val newSets = exerciseWithSets.sets
            val oldSets = getSetsFromExercise(exerciseId)

            // Create maps for fast O(1) lookups by ID.
            val oldSetsMap = oldSets.associateBy { it.id }
            val newSetsMap = newSets.associateBy { it.id }

            // Deletes from db the sets not found in the passed sets
            val idsToDelete = oldSetsMap.keys - newSetsMap.keys
            idsToDelete.forEach { idToDelete ->
                deleteSet(oldSetsMap.getValue(idToDelete))
            }

            newSets.forEach { set ->
                // Check if this set existed before.
                if (set.id in oldSetsMap) {
                    updateSet(set)
                } else {
                    addSet(set.copy(id = 0, exerciseId = exerciseId))
                }
            }
        }
    }
}