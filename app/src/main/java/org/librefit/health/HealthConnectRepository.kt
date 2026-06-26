/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_AVAILABLE
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.units.Mass
import androidx.health.connect.client.units.Percentage
import dagger.hilt.android.qualifiers.ApplicationContext
import org.librefit.db.entity.Measurement
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    val writePermissions: Set<String> = setOf(
        HealthPermission.getWritePermission(BodyFatRecord::class),
        HealthPermission.getWritePermission(WeightRecord::class)
    )

    private val healthConnectClient: HealthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    fun isAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == SDK_AVAILABLE
    }

    suspend fun hasWritePermissions(): Boolean {
        if (!isAvailable()) return false

        return healthConnectClient.permissionController
            .getGrantedPermissions()
            .containsAll(writePermissions)
    }

    suspend fun exportMeasurements(measurements: List<Measurement>): Int {
        if (!hasWritePermissions()) {
            throw SecurityException("Missing Health Connect write permissions")
        }

        val records = measurements.flatMap { it.toHealthConnectRecords() }
        if (records.isEmpty()) return 0

        healthConnectClient.insertRecords(records)
        return records.size
    }

    private fun Measurement.toHealthConnectRecords(): List<Record> {
        val zone = ZoneId.systemDefault()
        val zonedDate = date.atZone(zone)
        val instant = zonedDate.toInstant()
        val zoneOffset = zonedDate.offset
        val recordVersion = instant.toEpochMilli()

        return buildList {
            if (bodyWeight > 0.0) {
                add(
                    WeightRecord(
                        metadata = Metadata.manualEntry(
                            clientRecordId = "librefit-measurement-$id-body-weight",
                            clientRecordVersion = recordVersion
                        ),
                        time = instant,
                        zoneOffset = zoneOffset,
                        weight = Mass.kilograms(bodyWeight)
                    )
                )
            }

            if (bodyFatPercentage > 0) {
                add(
                    BodyFatRecord(
                        metadata = Metadata.manualEntry(
                            clientRecordId = "librefit-measurement-$id-body-fat",
                            clientRecordVersion = recordVersion
                        ),
                        time = instant,
                        zoneOffset = zoneOffset,
                        percentage = Percentage(bodyFatPercentage.toDouble())
                    )
                )
            }
        }
    }
}
