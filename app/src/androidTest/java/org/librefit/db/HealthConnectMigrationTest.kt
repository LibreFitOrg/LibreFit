/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.db

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.json.JSONObject
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class HealthConnectMigrationTest {
    @Test
    fun migratePublishedVersion5PreservesRoutineOrder() {
        migrateToVersion6(5)
    }

    @Test
    fun migrateVersion4IncludesRoutineOrderAndHealthConnect() {
        migrateToVersion6(4)
    }

    private fun migrateToVersion6(version: Int) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val name = "health-connect-migration-$version"
        context.deleteDatabase(name)
        try {
            val schema = instrumentation.context.assets
                .open("org.librefit.db.AppDatabase/$version.json")
                .bufferedReader().use { JSONObject(it.readText()).getJSONObject("database") }
            context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { db ->
                val entities = schema.getJSONArray("entities")
                for (index in 0 until entities.length()) {
                    val entity = entities.getJSONObject(index)
                    val tableName = entity.getString("tableName")
                    db.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", tableName))
                    val indices = entity.optJSONArray("indices") ?: JSONArray()
                    for (position in 0 until indices.length()) {
                        db.execSQL(indices.getJSONObject(position).getString("createSql")
                            .replace("\${TABLE_NAME}", tableName))
                    }
                }
                db.execSQL(
                    """
                    INSERT INTO workouts (id, routineId, notes, title, state, timeElapsed, created, completed)
                    VALUES (1, 7, 'saved notes', 'My routine', 'ROUTINE', 0, '2026-10-01T10:00', '2026-10-01T10:00')
                    """.trimIndent()
                )
                if (version == 5) db.execSQL("UPDATE workouts SET position = 3 WHERE id = 1")
                db.execSQL(
                    """
                    INSERT INTO dataset (id, name, level, primaryMuscles, secondaryMuscles, instructions, category, images, isCustomExercise)
                    VALUES ('custom-squat', 'Custom squat', 'BEGINNER', '[]', '[]', '[]', 'STRENGTH', '[]', 1)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO exercises (id, idExerciseDC, notes, setMode, restTime, position, workoutId)
                    VALUES (1, 'custom-squat', '', 'REPS', 60, 4, 1)
                    """.trimIndent()
                )
                db.execSQL("INSERT INTO sets (id, load, reps, elapsedTime, completed, exerciseId) VALUES (1, 20, 10, 0, 1, 1)")
                db.execSQL(
                    """
                    INSERT INTO measurements (id, bodyWeight, bodyFatPercentage, muscleMassPercentage, date, notes)
                    VALUES (1, 82.5, 17, 40, '2026-10-01T10:00', 'saved measurement')
                    """.trimIndent()
                )
                db.version = version
            }

            // Opening through Room validates the entire migrated schema against the entities.
            val database = Room.databaseBuilder(context, AppDatabase::class.java, name)
                .addMigrations(AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_5_6)
                .build()
            try {
                val db = database.openHelper.writableDatabase
                assertEquals(6, db.version)
                db.query("SELECT title, position, healthConnectRecordVersion FROM workouts WHERE id = 1")
                    .use { cursor ->
                        check(cursor.moveToFirst())
                        assertEquals("My routine", cursor.getString(0))
                        assertEquals(if (version == 5) 3 else 0, cursor.getInt(1))
                        assertEquals(0L, cursor.getLong(2))
                    }
                db.query("SELECT bodyWeight, healthConnectWeightRecordId, healthConnectBodyFatRecordId, healthConnectRecordVersion FROM measurements WHERE id = 1")
                    .use { cursor ->
                        check(cursor.moveToFirst())
                        assertEquals(82.5, cursor.getDouble(0))
                        assertNull(cursor.getString(1))
                        assertNull(cursor.getString(2))
                        assertEquals(0L, cursor.getLong(3))
                    }
                db.query("SELECT reps, startedAt, completedAt FROM sets WHERE id = 1")
                    .use { cursor ->
                        check(cursor.moveToFirst())
                        assertEquals(10, cursor.getInt(0))
                        assertNull(cursor.getString(1))
                        assertNull(cursor.getString(2))
                    }
                db.query("SELECT position FROM exercises WHERE id = 1").use { cursor ->
                    check(cursor.moveToFirst())
                    assertEquals(4, cursor.getInt(0))
                }
                db.query("SELECT healthConnectSegmentType FROM dataset WHERE id = 'custom-squat'")
                    .use { cursor ->
                        check(cursor.moveToFirst())
                        assertEquals("OTHER_WORKOUT", cursor.getString(0))
                    }
            } finally {
                database.close()
            }
        } finally {
            context.deleteDatabase(name)
        }
    }
}
