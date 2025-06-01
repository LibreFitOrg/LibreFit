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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.librefit.R
import org.librefit.data.ChartData
import org.librefit.db.entity.Measurement
import org.librefit.enums.MeasurementCardState
import org.librefit.enums.chart.MeasurementChart
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitButton
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.EmptyLottie
import org.librefit.ui.components.bottomMargin
import org.librefit.ui.components.charts.LibreFitCartesianChart
import org.librefit.ui.components.dialogs.ConfirmDialog
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter.formatDetails
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementScreen(
    navigateBack: () -> Unit
) {
    val viewModel: MeasurementScreenViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        viewModel.getMeasurementsFromDB()
    }


    val date = rememberSaveable { mutableStateOf(LocalDateTime.now()) }

    val datePickerState = rememberDatePickerState()
    val showDatePickerDialog = remember { mutableStateOf(false) }

    if (showDatePickerDialog.value == true) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        date.value = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(
                                datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                            ),
                            ZoneId.systemDefault()
                        )
                        showDatePickerDialog.value = false
                    }
                ) {
                    Text(stringResource(R.string.ok_dialog))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog.value = false }) {
                    Text(stringResource(R.string.cancel_dialog))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val measurementCardState = rememberSaveable { mutableStateOf(MeasurementCardState.NEW) }

    val idMeasurement = rememberSaveable { mutableLongStateOf(0L) }

    val showConfirmDialog = remember { mutableStateOf(false) }

    if (showConfirmDialog.value) {
        ConfirmDialog(
            title = stringResource(R.string.delete_measurement_question),
            text = stringResource(R.string.delete_measurement_text),
            confirmText = stringResource(R.string.delete),
            onConfirm = {
                viewModel.deleteMeasurementById(idMeasurement.longValue)
                idMeasurement.longValue = 0L
                showConfirmDialog.value = false
                measurementCardState.value = MeasurementCardState.NEW
            },
            onDismiss = {
                showConfirmDialog.value = false
            }
        )
    }

    MeasurementScreenContent(
        measurements = viewModel.measurements,
        listChartData = viewModel.getListChartData(),
        idMeasurement = idMeasurement,
        date = date,
        showDatePickerDialog = showDatePickerDialog,
        showConfirmDialog = showConfirmDialog,
        measurementCardState = measurementCardState,
        upsertMeasurement = viewModel::upsertMeasurementToDB,
        updateChartMode = viewModel::updateMeasurementChart,
        measurementChart = viewModel.getMeasurementChart(),
        navigateBack = navigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeasurementScreenContent(
    measurements: List<Measurement>,
    listChartData: List<ChartData>,
    idMeasurement: MutableLongState,
    date: MutableState<LocalDateTime>,
    measurementChart: MeasurementChart,
    showDatePickerDialog: MutableState<Boolean>,
    showConfirmDialog: MutableState<Boolean>,
    measurementCardState: MutableState<MeasurementCardState>,
    upsertMeasurement: (Measurement) -> Unit,
    updateChartMode: (MeasurementChart) -> Unit,
    navigateBack: () -> Unit,
) {
    val fullDate: DateTimeFormatter? = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.FULL)
        .withLocale(Locale.getDefault())

    val shortDate: DateTimeFormatter? = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.SHORT)
        .withLocale(Locale.getDefault())


    var bodyWeight by rememberSaveable { mutableStateOf("") }
    var fatMass by rememberSaveable { mutableStateOf("") }
    var leanMass by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }

    var trigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(measurementCardState.value, trigger) {
        if (measurementCardState.value == MeasurementCardState.NEW) {
            idMeasurement.longValue = 0L
            bodyWeight = ""
            fatMass = ""
            leanMass = ""
            notes = ""
            date.value = LocalDateTime.now()
        }
    }


    val focusManager = LocalFocusManager.current

    val focusRequester = remember { FocusRequester() }
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LibreFitScaffold(
        title = AnnotatedString(stringResource(R.string.measurements)),
        navigateBack = navigateBack
    ) { innerPadding ->
        LibreFitLazyColumn(innerPadding, lazyListState = lazyListState) {
            item {
                LibreFitCartesianChart(
                    format = when (measurementChart) {
                        MeasurementChart.BODY_WEIGHT -> DecimalFormat("# " + stringResource(R.string.kg))
                        MeasurementChart.FAT_MASS -> DecimalFormat("0' %'")
                        MeasurementChart.LEAN_MASS -> DecimalFormat("0' %'")
                    },
                    listChartData = listChartData,
                    chartMode = measurementChart,
                    updateChartMode = { updateChartMode(it as MeasurementChart) }
                )
            }

            // Add/edit measurement card
            item {
                var isExpanded by rememberSaveable { mutableStateOf(false) }

                OutlinedCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(
                                    if (measurementCardState.value == MeasurementCardState.NEW)
                                        R.string.new_measurement else R.string.edit_measurement
                                ),
                                style = MaterialTheme.typography.headlineSmall
                            )

                            IconButton(
                                onClick = {
                                    isExpanded = !isExpanded
                                }
                            ) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                            }
                        }

                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            value = bodyWeight,
                            label = { Text(text = stringResource(R.string.body_weight) + " *") },
                            suffix = { Text(stringResource(R.string.kg)) },
                            isError = bodyWeight.isBlank(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                showKeyboardOnFocus = true
                            ),
                            onValueChange = {
                                bodyWeight = processFloatValue(it, 300f)
                            }
                        )
                        AnimatedVisibility(visible = isExpanded) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = fatMass,
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { fatMass = "" }
                                        ) {
                                            Icon(
                                                ImageVector.vectorResource(R.drawable.ic_cancel),
                                                null
                                            )
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(R.string.fat_mass),
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    suffix = { Text("%") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    onValueChange = {
                                        fatMass = processFloatValue(it, 100f)
                                    }
                                )

                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = leanMass,
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { leanMass = "" }
                                        ) {
                                            Icon(
                                                ImageVector.vectorResource(R.drawable.ic_cancel),
                                                null
                                            )
                                        }
                                    },
                                    label = { Text(stringResource(R.string.lean_mass)) },
                                    suffix = { Text("%") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    onValueChange = {
                                        leanMass = processFloatValue(it, 100f)
                                    }
                                )
                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = date.value.format(shortDate),
                                    onValueChange = {},
                                    label = { Text(stringResource(R.string.label_when)) },
                                    readOnly = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            showDatePickerDialog.value = true
                                        }) {
                                            Icon(
                                                imageVector = ImageVector.vectorResource(R.drawable.ic_date_range),
                                                contentDescription = stringResource(R.string.select_date)
                                            )
                                        }
                                    }
                                )

                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = notes,
                                    label = { Text(stringResource(R.string.notes)) },
                                    onValueChange = { notes = it }
                                )
                            }
                        }

                        Row {
                            LibreFitButton(
                                modifier = Modifier.weight(1f),
                                text = stringResource(
                                    if (measurementCardState.value == MeasurementCardState.NEW)
                                        R.string.add else R.string.save
                                ),
                                icon = ImageVector.vectorResource(
                                    if (measurementCardState.value == MeasurementCardState.NEW)
                                        R.drawable.ic_add else R.drawable.ic_edit
                                ),
                                enabled = bodyWeight.isNotBlank()
                            ) {
                                upsertMeasurement(
                                    Measurement(
                                        id = idMeasurement.longValue,
                                        bodyWeight = bodyWeight.toFloat(),
                                        bodyFatPercentage = fatMass.ifBlank { "0" }.toFloat(),
                                        muscleMassPercentage = leanMass.ifBlank { "0" }
                                            .toFloat(),
                                        date = date.value,
                                        notes = notes
                                    )
                                )

                                measurementCardState.value = MeasurementCardState.NEW

                                focusManager.clearFocus()

                                trigger++
                            }
                            AnimatedVisibility(measurementCardState.value == MeasurementCardState.EDIT) {
                                IconButton(
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        measurementCardState.value = MeasurementCardState.NEW
                                        focusManager.clearFocus()
                                    }
                                ) {
                                    Icon(ImageVector.vectorResource(R.drawable.ic_cancel), null)
                                }
                            }
                        }
                    }
                }
            }


            item { HeadlineText(stringResource(R.string.past_measurement)) }

            if (measurements.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        EmptyLottie()
                        Text(
                            text = stringResource(R.string.nothing_to_show),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            items(measurements, key = { it.id }) {
                ElevatedCard(Modifier.animateItem()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = it.date.format(fullDate)
                                        .replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    text = "${it.bodyWeight} " + stringResource(R.string.kg),
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row {
                                IconButton(
                                    onClick = {
                                        idMeasurement.longValue = it.id
                                        bodyWeight = it.bodyWeight.toString()
                                        leanMass = it.muscleMassPercentage
                                            .toString().takeIf { it != "0.0" } ?: ""
                                        fatMass = it.bodyFatPercentage
                                            .toString().takeIf { it != "0.0" } ?: ""
                                        notes = it.notes
                                        date.value = it.date
                                        measurementCardState.value =
                                            MeasurementCardState.EDIT


                                        coroutineScope.launch {
                                            lazyListState.animateScrollToItem(index = 1)
                                            focusRequester.requestFocus()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_edit),
                                        contentDescription = null
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        showConfirmDialog.value = true
                                        idMeasurement.longValue = it.id
                                    }
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_delete),
                                        contentDescription = null
                                    )
                                }
                            }
                        }

                        if (it.muscleMassPercentage != 0f || it.bodyFatPercentage != 0f) {
                            HorizontalDivider()
                        }

                        if (it.muscleMassPercentage != 0f) {
                            Text(
                                formatDetails(
                                    stringResource(R.string.lean_mass),
                                    it.muscleMassPercentage.toString() + " %"
                                )
                            )
                        }
                        if (it.bodyFatPercentage != 0f) {
                            Text(
                                formatDetails(
                                    stringResource(R.string.fat_mass),
                                    it.bodyFatPercentage.toString() + " %"
                                )
                            )
                        }

                        if (it.notes != "") {
                            HorizontalDivider()
                            Text(
                                formatDetails(stringResource(R.string.notes), it.notes)
                            )
                        }
                    }
                }
            }
            bottomMargin()
        }
    }
}

private fun processFloatValue(string: String, max: Float): String {
    if (string.isBlank()) return ""

    val stringValue = string
        .replace(",", ".")
        .filter { it.isDigit() || it == '.' }
        .takeLast(5)

    val firstDotIndex = stringValue.indexOf(".")

    var value: String

    if (firstDotIndex == -1) {
        value = stringValue
    } else {
        val beforeFirstDot = stringValue.substring(
            0, firstDotIndex + 1
        )

        val afterFirstDot = stringValue
            .substring(firstDotIndex + 1)
            .replace(".", "")

        value = beforeFirstDot + afterFirstDot
    }

    if (value == ".") value = "0"

    return value.toFloat().coerceAtMost(maximumValue = max).toString()
}

@Preview
@Composable
private fun MeasurementScreenPreview() {
    val shortDate: DateTimeFormatter? = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.SHORT)
        .withLocale(Locale.getDefault())

    // Used to generate random dates
    val from = LocalDateTime.of(2025, 1, 1, 0, 0)
    val to = LocalDateTime.of(2025, 12, 31, 23, 59)

    val fromEpochSecond = from.toEpochSecond(ZoneOffset.UTC)
    val toEpochSecond = to.toEpochSecond(ZoneOffset.UTC)


    val measurements = (0 until 10)
        .map {
            Measurement(
                id = it.toLong(),
                notes = if (Random.nextBoolean()) "This is the note of the ${it + 1}° measurement" else "",
                bodyWeight = Random.nextLong(60, 80).toFloat(),
                bodyFatPercentage = if (Random.nextBoolean()) Random.nextLong(10, 80)
                    .toFloat() else 0f,
                muscleMassPercentage = if (Random.nextBoolean()) Random.nextLong(20, 80)
                    .toFloat() else 0f,
                date = LocalDateTime.ofEpochSecond(
                    Random.nextLong(fromEpochSecond, toEpochSecond),
                    0,
                    ZoneOffset.UTC
                )
            )
        }
        .sortedByDescending { it.date }

    val measurementChart = MeasurementChart.entries.random()

    LibreFitTheme(false, true) {
        MeasurementScreenContent(
            measurements = measurements,
            listChartData = measurements.map {
                ChartData(
                    yValue = when (measurementChart) {
                        MeasurementChart.BODY_WEIGHT -> it.bodyWeight
                        MeasurementChart.FAT_MASS -> it.bodyFatPercentage
                        MeasurementChart.LEAN_MASS -> it.muscleMassPercentage
                    },
                    xValue = it.date.format(shortDate)
                )
            },
            idMeasurement = remember { mutableLongStateOf(0L) },
            date = remember { mutableStateOf(LocalDateTime.now()) },
            measurementChart = measurementChart,
            showDatePickerDialog = remember { mutableStateOf(false) },
            showConfirmDialog = remember { mutableStateOf(false) },
            measurementCardState = remember { mutableStateOf(MeasurementCardState.NEW) },
            upsertMeasurement = {},
            updateChartMode = {},
            navigateBack = {}
        )
    }
}