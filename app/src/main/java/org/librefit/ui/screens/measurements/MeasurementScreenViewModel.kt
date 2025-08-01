/*
 * Copyright (c) 2025. LibreFit
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

package org.librefit.ui.screens.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.librefit.db.entity.Measurement
import org.librefit.db.repository.MeasurementRepository
import org.librefit.enums.MeasurementCardState
import org.librefit.enums.chart.MeasurementChart
import org.librefit.ui.components.charts.ChartData
import org.librefit.util.Formatter
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MeasurementScreenViewModel @Inject constructor(
    private val measurementRepository: MeasurementRepository
) : ViewModel() {
    private val _measurementChart = MutableStateFlow(MeasurementChart.BODY_WEIGHT)
    val measurementChart = _measurementChart.asStateFlow()

    fun updateMeasurementChart(newMeasurementChart: MeasurementChart) {
        _measurementChart.value = newMeasurementChart
    }


    val measurements: StateFlow<List<Measurement>> = measurementRepository.measurements
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    val listChartData: StateFlow<List<ChartData>> =
        combine(
            measurements,
            measurementChart
        ) { measurements, measurementChart ->
            measurements
                .filter {
                    when (measurementChart) {
                        MeasurementChart.BODY_WEIGHT -> it.bodyWeight != 0f
                        MeasurementChart.FAT_MASS -> it.bodyFatPercentage != 0f
                        MeasurementChart.LEAN_MASS -> it.muscleMassPercentage != 0f
                    }
                }
                .map {
                    ChartData(
                        yValues = listOf(
                            when (measurementChart) {
                                MeasurementChart.BODY_WEIGHT -> it.bodyWeight
                                MeasurementChart.FAT_MASS -> it.bodyFatPercentage
                                MeasurementChart.LEAN_MASS -> it.muscleMassPercentage
                            }
                        ),
                        xValue = Formatter.getShortDateFromLocalDate(it.date)
                    )
                }
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )


    private val _idMeasurement = MutableStateFlow(0L)
    val idMeasurement = _idMeasurement.asStateFlow()

    fun updateIdMeasurement(newValue: Long) {
        _idMeasurement.value = newValue
    }


    private val _bodyweight = MutableStateFlow(0f)
    val bodyWeight = _bodyweight.asStateFlow()

    fun updateBodyweight(newValue: String) {
        _bodyweight.value = newValue.ifBlank { "0" }.toFloat()
    }


    private val _fatMass = MutableStateFlow(0f)
    val fatMass = _fatMass.asStateFlow()

    fun updateFatMass(newValue: String) {
        _fatMass.value = newValue.ifBlank { "0" }.toFloat()
    }


    private val _leanMass = MutableStateFlow(0f)
    val leanMass = _leanMass.asStateFlow()

    fun updateLeanMass(newValue: String) {
        _leanMass.value = newValue.ifBlank { "0" }.toFloat()
    }


    private val _notes = MutableStateFlow("")
    val notes = _notes.asStateFlow()

    fun updateNotes(newValue: String) {
        _notes.value = newValue
    }


    private val _date = MutableStateFlow(LocalDateTime.now())
    val date = _date.asStateFlow()

    fun updateDate(newValue: LocalDateTime) {
        _date.value = newValue
    }


    private val _measurementCardState = MutableStateFlow(MeasurementCardState.NEW)
    val measurementCardState = _measurementCardState.asStateFlow()

    fun updateMeasurementCardState(measurementCardState: MeasurementCardState) {
        _measurementCardState.value = measurementCardState
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
                _notes.value = measurement.notes
                _bodyweight.value = measurement.bodyWeight
                _leanMass.value = measurement.muscleMassPercentage
                _fatMass.value = measurement.bodyFatPercentage
                _date.value = measurement.date
            }
        }
    }


    fun upsertMeasurementToDB() {
        viewModelScope.launch {
            measurementRepository.upsertMeasurement(
                Measurement(
                    id = if (measurementCardState.value == MeasurementCardState.EDIT)
                        idMeasurement.value else 0L,
                    bodyWeight = bodyWeight.value,
                    notes = notes.value,
                    muscleMassPercentage = leanMass.value,
                    bodyFatPercentage = fatMass.value,
                    date = date.value
                )
            )

            _measurementCardState.value = MeasurementCardState.NEW
        }
    }

    fun deleteMeasurementById(id: Long) {
        viewModelScope.launch {
            measurementRepository.deleteById(id)

            _measurementCardState.value = MeasurementCardState.NEW
        }
    }
}