package org.librefit.db.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.librefit.db.ExportSchema
import org.librefit.db.dao.MeasurementDao
import org.librefit.db.dao.WorkoutDao
import org.librefit.db.entity.Measurement
import org.librefit.db.entity.Workout
import org.librefit.db.importExport.dto.ExportData
import org.librefit.db.importExport.dto.ExportPayload
import org.librefit.db.importExport.mapper.toExport
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.models.Weight
import java.io.ByteArrayInputStream
import kotlin.test.Test

class ImportExportRepositoryTest {
    private val workoutDao = mockk<WorkoutDao>(relaxed = true)
    private val measurementDao = mockk<MeasurementDao>(relaxed = true)
    private val repository = ImportExportRepository(
        workoutDao = workoutDao,
        measurementDao = measurementDao,
        ioDispatcher = Dispatchers.Unconfined,
    )

    @Test
    fun `importing matching records keeps existing data unchanged`() = runTest {
        val workout = Workout(id = 12, routineId = 12, title = "Existing workout")
        val workoutRelation = WorkoutWithExercisesAndSets(workout, emptyList())
        val measurement = Measurement(
            id = 7,
            bodyWeight = Weight.kilograms(72.5),
            notes = "Existing measurement",
        )
        coEvery { workoutDao.getAllWorkoutsWithExercisesAndSetsOnce() } returns listOf(workoutRelation)
        coEvery { measurementDao.getAllMeasurementsOnce() } returns listOf(measurement)

        val payload = ExportPayload(
            schemaVersion = ExportSchema.VERSION,
            data = ExportData(
                workouts = listOf(workoutRelation.toExport()),
                measurements = listOf(measurement.toExport()),
            ),
        )
        val input = Json.encodeToString(payload).byteInputStream()

        repository.importFrom(ByteArrayInputStream(input.readBytes()))

        coVerify(exactly = 0) { workoutDao.addWorkoutWithExercisesAndSets(any()) }
        coVerify(exactly = 0) { measurementDao.upsertMeasurement(any()) }
    }
}
