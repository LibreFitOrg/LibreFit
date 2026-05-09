package org.librefit.db.importExport.mapper

import org.librefit.db.entity.Exercise
import org.librefit.db.entity.Workout
import org.librefit.db.entity.Set
import org.librefit.db.importExport.dto.ExportWorkout
import org.librefit.db.relations.ExerciseWithSets
import org.librefit.db.relations.WorkoutWithExercisesAndSets

fun ExportWorkout.toRelation(): WorkoutWithExercisesAndSets {
    val workoutEntity = Workout(
        id = 0, // 0 -> then new unique ID assigned by ROOM
        routineId = routineId,
        notes = notes,
        title = title,
        state = state,
        timeElapsed = timeElapsed,
        created = created,
        completed = completed
    )

    val exerciseRelations = exercises.map { ex ->
        val exerciseEntity = Exercise(
            id = 0, // 0 -> then new unique ID assigned by ROOM
            idExerciseDC = ex.idExerciseDC,
            notes = ex.notes,
            setMode = ex.setMode,
            restTime = ex.restTime,
            position = ex.position,
            workoutId = id
        )

        val sets = ex.sets.map { s ->
            Set(
                id = 0, // 0 -> then new unique ID assigned by ROOM
                load = s.load,
                reps = s.reps,
                elapsedTime = s.elapsedTime,
                completed = s.completed,
                exerciseId = ex.id
            )
        }

        ExerciseWithSets(
            exercise = exerciseEntity,
            sets = sets
        )
    }

    return WorkoutWithExercisesAndSets(
        workout = workoutEntity,
        exercisesWithSets = exerciseRelations
    )
}