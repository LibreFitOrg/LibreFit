package org.librefit.db.importExport.mapper

import org.librefit.db.entity.Measurement
import org.librefit.db.importExport.dto.ExportExercise
import org.librefit.db.importExport.dto.ExportMeasurement
import org.librefit.db.importExport.dto.ExportSet
import org.librefit.db.importExport.dto.ExportWorkout
import org.librefit.db.relations.WorkoutWithExercisesAndSets

fun WorkoutWithExercisesAndSets.toExport(): ExportWorkout =
    ExportWorkout(
        id = workout.id,
        routineId = workout.routineId,
        notes = workout.notes,
        title = workout.title,
        state = workout.state,
        timeElapsed = workout.timeElapsed,
        position = workout.position,
        created = workout.created,
        completed = workout.completed,
        exercises = exercisesWithSets.map { exerciseWithSets ->
            ExportExercise(
                id = exerciseWithSets.exercise.id,
                idExerciseDC = exerciseWithSets.exercise.idExerciseDC,
                notes = exerciseWithSets.exercise.notes,
                setMode = exerciseWithSets.exercise.setMode,
                restTime = exerciseWithSets.exercise.restTime,
                position = exerciseWithSets.exercise.position,
                workoutId = exerciseWithSets.exercise.workoutId,
                sets = exerciseWithSets.sets.map { set ->
                    ExportSet(
                        id = set.id,
                        load = set.load.inKilograms,
                        reps = set.reps,
                        elapsedTime = set.elapsedTime,
                        completed = set.completed,
                        exerciseId = set.exerciseId,
                    )
                },
            )
        },
    )

fun Measurement.toExport(): ExportMeasurement =
    ExportMeasurement(
        id = id,
        bodyWeight = bodyWeight.inKilograms,
        bodyFatPercentage = bodyFatPercentage,
        muscleMassPercentage = muscleMassPercentage,
        date = date,
        notes = notes,
    )

fun ExportWorkout.importIdentity(): ExportWorkout =
    copy(
        id = 0,
        exercises = exercises.map { exercise ->
            exercise.copy(
                id = 0,
                workoutId = 0,
                sets = exercise.sets.map { set ->
                    set.copy(id = 0, exerciseId = 0)
                },
            )
        },
    )

fun ExportMeasurement.importIdentity(): ExportMeasurement = copy(id = 0)
