/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.librefit.db.entity.Measurement
import org.librefit.db.repository.MeasurementRepository
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.di.qualifiers.DefaultDispatcher
import org.librefit.enums.MeasurementCardState
import org.librefit.enums.chart.MeasurementChart
import org.librefit.ui.components.charts.Point
import org.librefit.util.Formatter
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MeasurementScreenViewModel @Inject constructor(
    private val measurementRepository: MeasurementRepository,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    val useNumberPicker = userPreferencesRepository.useNumberPicker


    private val _measurementChart = MutableStateFlow(MeasurementChart.BODY_WEIGHT)
    val measurementChart = _measurementChart.asStateFlow()

    fun updateMeasurementChart(newMeasurementChart: MeasurementChart) {
        _measurementChart.update { newMeasurementChart }
    }


    val measurements: StateFlow<List<Measurement>> = measurementRepository.measurements
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    val points: StateFlow<List<Point>> =
        combine(
            measurements,
            measurementChart
        ) { measurements, measurementChart ->
            measurements
                .filter {
                    when (measurementChart) {
                        MeasurementChart.BODY_WEIGHT -> it.bodyWeight != 0.0
                        MeasurementChart.FAT_MASS -> it.bodyFatPercentage != 0
                        MeasurementChart.LEAN_MASS -> it.muscleMassPercentage != 0
                    }
                }
                .map {
                    Point(
                        yValues = listOf(
                            when (measurementChart) {
                                MeasurementChart.BODY_WEIGHT -> it.bodyWeight
                                MeasurementChart.FAT_MASS -> it.bodyFatPercentage
                                MeasurementChart.LEAN_MASS -> it.muscleMassPercentage
                            }.toDouble()
                        ),
                        xValue = Formatter.getShortDateFromLocalDate(it.date)
                    )
                }
        }
            .distinctUntilChanged()
            .flowOn(defaultDispatcher)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )


    private val _idMeasurement = MutableStateFlow(0L)
    val idMeasurement = _idMeasurement.asStateFlow()

    fun updateIdMeasurement(newValue: Long) {
        _idMeasurement.update { newValue }
    }


    private val _bodyweight = MutableStateFlow("")
    val bodyWeight = _bodyweight.asStateFlow()

    fun updateBodyweight(newValue: String) {
        _bodyweight.update {
            val value = Formatter.normalizeNumericString(newValue)

            if (value.isEmpty()) {
                value
            } else if (value.indexOf(".") == -1) {
                // Integer
                value.toInt().coerceIn(0, 300).toString()
            } else {
                // Double
                value.toDouble().coerceIn(0.0, 300.0).toString()
            }
        }
    }


    private val _fatMass = MutableStateFlow<Int?>(null)
    val fatMass = _fatMass.asStateFlow()

    fun updateFatMass(newValue: String) {
        _fatMass.update {
            Formatter.parseIntegerFromString(string = newValue, maxValue = 100, minValue = 0)
        }
    }


    private val _leanMass = MutableStateFlow<Int?>(null)
    val leanMass = _leanMass.asStateFlow()

    fun updateLeanMass(newValue: String) {
        _leanMass.update {
            Formatter.parseIntegerFromString(string = newValue, maxValue = 100, minValue = 0)
        }
    }


    private val _notes = MutableStateFlow("")
    val notes = _notes.asStateFlow()

    fun updateNotes(newValue: String) {
        _notes.update { newValue }
    }


    private val _date = MutableStateFlow(LocalDateTime.now())
    val date = _date.asStateFlow()

    fun updateDate(newValue: LocalDateTime) {
        _date.update { newValue }
    }


    private val _measurementCardState = MutableStateFlow(MeasurementCardState.NEW)
    val measurementCardState = _measurementCardState.asStateFlow()

    fun updateMeasurementCardState(measurementCardState: MeasurementCardState) {
        _measurementCardState.update { measurementCardState }
    }


    private val currentMeasurement: StateFlow<Measurement> =
        combine(idMeasurement, measurements, measurementCardState) { id, m, mcs ->
            if (mcs == MeasurementCardState.EDIT) {
                m.find { it.id == id } ?: Measurement()
            } else Measurement()
        }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Measurement() // Start with null until the first ID is processed
            )

    init {
        viewModelScope.launch {
            // A new current measurement is emitted when idMeasurement changes and MeasurementCardState is EDIT
            currentMeasurement.collect { measurement ->
                _notes.update { measurement.notes }
                _bodyweight.update { measurement.bodyWeight.takeIf { it != 0.0 }?.toString() ?: "" }
                _leanMass.update { measurement.muscleMassPercentage.takeIf { it != 0 } }
                _fatMass.update { measurement.bodyFatPercentage.takeIf { it != 0 } }
                _date.update { measurement.date }
            }
        }
    }


    fun upsertMeasurementToDB() {
        viewModelScope.launch {
            measurementRepository.upsertMeasurement(
                Measurement(
                    id = if (measurementCardState.value == MeasurementCardState.EDIT)
                        idMeasurement.value else 0L,
                    bodyWeight = checkNotNull(bodyWeight.value.toDoubleOrNull()) {
                        "Bodyweight must be a double when saving a new measurement. Actual value: ${bodyWeight.value}"
                    },
                    notes = notes.value,
                    muscleMassPercentage = leanMass.value ?: 0,
                    bodyFatPercentage = fatMass.value ?: 0,
                    date = date.value
                )
            )

            _measurementCardState.update { MeasurementCardState.NEW }
        }
    }

    fun deleteMeasurementById(id: Long) {
        viewModelScope.launch {
            measurementRepository.deleteById(id)

            _measurementCardState.update { MeasurementCardState.NEW }
        }
    }
}