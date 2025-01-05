/*
 * Copyright (c) 2024-2025. LibreFit
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

package org.librefit.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.librefit.util.ExerciseWithSets
import java.time.LocalDateTime

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts WHERE 1 = routine ORDER BY title" )
    fun getRoutines() : Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE 0 = routine ORDER BY completed")
    fun getCompletedWorkouts(): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE id = :id")
    fun getWorkout(id: Int): Workout

    @Insert
    fun addWorkout(workout: Workout) : Long

    @Update
    fun updateWorkout(workout: Workout)

    @Delete
    fun deleteWorkout(workout: Workout)

    @Insert
    fun addExercise(exercise: Exercise) : Long

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Insert
    suspend fun addSet(set: Set)

    @Delete
    suspend fun deleteSet(set: Set)

    @Query("SELECT * FROM exercises WHERE workoutId = :workoutId")
    suspend fun getExercisesFromWorkout(workoutId: Int): List<Exercise>

    @Query("SELECT * FROM sets WHERE exerciseId = :exerciseId")
    suspend fun getSetsFromExercise(exerciseId : Int): List<Set>

    @Transaction
    suspend fun addWorkoutWithExercises(workout: Workout, exercises: List<ExerciseWithSets>) {
        val workoutId = if (workout.routine) {
            addWorkout(workout.copy(id = 0, completed = LocalDateTime.now())).toInt()
        } else {
            addWorkout(workout.copy(id = 0, created = LocalDateTime.now())).toInt()
        }
        exercises.forEach {
            val exerciseId = addExercise(
                Exercise(
                    exerciseId = it.exerciseDC.id,
                    notes = it.note,
                    workoutId = workoutId,
                    setMode = it.setMode,
                    restTime = it.restTime
                )
            )
            it.sets.forEach { set ->
                addSet(
                    set.copy(
                        id = 0,
                        exerciseId = exerciseId.toInt()
                    )
                )
            }
        }
    }
}