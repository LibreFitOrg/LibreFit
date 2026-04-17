/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
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
import org.librefit.enums.WorkoutState
import java.time.LocalDateTime

@Dao
interface WorkoutDao {
    /**
     * Returns a flow that emits a stream of [org.librefit.db.entity.Workout]s filtered by [state]
     */
    @Query("SELECT * FROM workouts WHERE state = :state ORDER BY created")
    fun getWorkoutsByState(state: WorkoutState): Flow<List<Workout>>

    @Transaction
    @Query("SELECT * FROM workouts WHERE state = :state ORDER BY created")
    fun getWorkoutsWithExercisesAndSetsByState(state: WorkoutState): Flow<List<WorkoutWithExercisesAndSets>>

    /**
     * Returns a flow that emits a stream of [Workout]s which have the requested [state]. They are
     * ordered by date from newest to latest ([Workout.completed])
     */
    @Query("SELECT * FROM workouts WHERE state = :state ORDER BY completed DESC")
    fun getWorkoutsByStateAndOrderedByCompleted(state: WorkoutState): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkout(id: Long): Workout

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkoutWithExercisesAndSets(id: Long): WorkoutWithExercisesAndSets

    @Query("SELECT * FROM workouts WHERE state = :state AND routineId = :routineId")
    suspend fun getWorkoutFromRoutineIDAndState(routineId: Long, state: WorkoutState): Workout?

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
     * Returns a flow list [org.librefit.db.relations.WorkoutWithExercisesAndSets]s filtered by [state].
     * They are  ordered by date from newest to latest ([Workout.completed])
     */
    @Transaction
    @Query("SELECT * FROM workouts WHERE state = :state ORDER BY completed DESC")
    fun getWorkoutsWithExercisesAndSetsByStateAndOrderedByCompleted(state: WorkoutState): Flow<List<WorkoutWithExercisesAndSets>>


    /**
     * Retrieves the list of [org.librefit.db.relations.ExerciseWithSets] associated with a specific workout.
     * This function queries the database to fetch all exercises that belong to the given [Exercise.workoutId]
     */
    @Transaction
    @Query("SELECT * FROM exercises WHERE workoutId = :workoutId ORDER BY position, id")
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
    @Query("SELECT * FROM workouts WHERE routineId = :routineId AND state = :state ORDER BY completed DESC")
    suspend fun getWorkoutsWithExercisesAndSetsFromRoutineByState(
        routineId: Long,
        state: WorkoutState
    ): List<WorkoutWithExercisesAndSets>

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
    ): Long {
        val workout = workoutWithExercisesAndSets.workout

        val workoutId = if (workout.id == 0L) {
            addWorkout(
                workout = when (workout.state) {
                    WorkoutState.ROUTINE -> workout.copy(completed = LocalDateTime.now())
                    else -> workout.copy(created = LocalDateTime.now())
                }
            )
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
        return workoutId
    }

    /**
     * It provides all [WorkoutWithExercisesAndSets] having the passed [org.librefit.db.entity.ExerciseDC] in [Exercise] and
     * the passed [state]
     */
    @Transaction
    @Query(
        """
        SELECT * FROM workouts 
        WHERE id IN (
            SELECT DISTINCT workoutId FROM exercises WHERE idExerciseDC = :idExerciseDC
        ) AND state = :state ORDER BY completed DESC
    """
    )
    fun getWorkoutsFromIdExerciseDC(
        idExerciseDC: String,
        state: WorkoutState
    ): Flow<List<WorkoutWithExercisesAndSets>>

}
