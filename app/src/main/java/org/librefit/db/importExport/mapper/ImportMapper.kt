package org.librefit.db.importExport.mapper

import org.librefit.db.entity.Exercise
import org.librefit.db.entity.Measurement
import org.librefit.db.entity.Set
import org.librefit.db.entity.Workout
import org.librefit.db.importExport.dto.ExportMeasurement
import org.librefit.db.importExport.dto.ExportWorkout
import org.librefit.db.relations.ExerciseWithSets
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.models.Weight

fun ExportWorkout.toRelation(): WorkoutWithExercisesAndSets {
    val workoutEntity = Workout(
        id = 0,
        routineId = routineId,
        notes = notes,
        title = title,
        state = state,
        timeElapsed = timeElapsed,
        position = position,
        created = created,
        completed = completed,
    )

    val exerciseRelations = exercises.map { exercise ->
        val exerciseEntity = Exercise(
            id = 0,
            idExerciseDC = exercise.idExerciseDC,
            notes = exercise.notes,
            setMode = exercise.setMode,
            restTime = exercise.restTime,
            position = exercise.position,
            workoutId = 0,
        )

        val sets = exercise.sets.map { set ->
            Set(
                id = 0,
                load = Weight.kilograms(set.load),
                reps = set.reps,
                elapsedTime = set.elapsedTime,
                completed = set.completed,
                exerciseId = 0,
            )
        }

        ExerciseWithSets(exercise = exerciseEntity, sets = sets)
    }

    return WorkoutWithExercisesAndSets(
        workout = workoutEntity,
        exercisesWithSets = exerciseRelations,
    )
}

fun ExportMeasurement.toEntity(): Measurement = Measurement(
    id = 0,
    bodyWeight = Weight.kilograms(bodyWeight),
    bodyFatPercentage = bodyFatPercentage,
    muscleMassPercentage = muscleMassPercentage,
    date = date,
    notes = notes,
)
