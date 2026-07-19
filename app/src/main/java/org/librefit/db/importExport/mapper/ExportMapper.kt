package org.librefit.db.importExport.mapper

import org.librefit.db.importExport.dto.ExportExercise
import org.librefit.db.importExport.dto.ExportSet
import org.librefit.db.importExport.dto.ExportWorkout
import org.librefit.db.relations.WorkoutWithExercisesAndSets

fun WorkoutWithExercisesAndSets.toExport(): ExportWorkout {
    return ExportWorkout(
        id = workout.id,
        routineId = workout.routineId,
        notes = workout.notes,
        title = workout.title,
        state = workout.state,
        timeElapsed = workout.timeElapsed,
        created = workout.created,
        completed = workout.completed,
        exercises = exercisesWithSets.map { ex ->
            ExportExercise(
                id = ex.exercise.id,
                idExerciseDC = ex.exercise.idExerciseDC,
                notes = ex.exercise.notes,
                setMode = ex.exercise.setMode,
                restTime = ex.exercise.restTime,
                position = ex.exercise.position,
                workoutId = ex.exercise.workoutId,
                sets = ex.sets.map { s ->
                    ExportSet(
                        id = s.id,
                        load = s.load,
                        reps = s.reps,
                        elapsedTime = s.elapsedTime,
                        completed = s.completed,
                        exerciseId = s.exerciseId
                    )
                }
            )
        }
    )
}