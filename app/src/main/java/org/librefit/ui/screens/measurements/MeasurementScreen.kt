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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import org.librefit.db.entity.Measurement
import org.librefit.enums.MeasurementCardState
import org.librefit.enums.chart.MeasurementChart
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitButton
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.EmptyLottie
import org.librefit.ui.components.bottomMargin
import org.librefit.ui.components.charts.ChartData
import org.librefit.ui.components.charts.LibreFitCartesianChart
import org.librefit.ui.components.dialogs.ConfirmDialog
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter
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

    val measurements by viewModel.measurements.collectAsState()

    val listChartData by viewModel.listChartData.collectAsState()

    val measurementChart by viewModel.measurementChart.collectAsState()

    val bodyweight by viewModel.bodyWeight.collectAsState()

    val leanMass by viewModel.leanMass.collectAsState()

    val fatMass by viewModel.fatMass.collectAsState()

    val notes by viewModel.notes.collectAsState()

    val measurementCardState by viewModel.measurementCardState.collectAsState()

    val date by viewModel.date.collectAsState()

    val datePickerState = rememberDatePickerState()
    var showDatePickerDialog by remember { mutableStateOf(false) }

    if (showDatePickerDialog) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateDate(
                            newValue = LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(
                                    datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                                ),
                                ZoneId.systemDefault()
                            )
                        )
                        showDatePickerDialog = false
                    }
                ) {
                    Text(stringResource(R.string.ok_dialog))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text(stringResource(R.string.cancel_dialog))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    var showConfirmDialog by remember { mutableStateOf(false) }

    var idMeasurementToDelete by remember { mutableLongStateOf(0L) }

    if (showConfirmDialog) {
        ConfirmDialog(
            title = stringResource(R.string.delete_measurement_question),
            text = stringResource(R.string.delete_measurement_text),
            confirmText = stringResource(R.string.delete),
            onConfirm = {
                viewModel.deleteMeasurementById(idMeasurementToDelete)
                showConfirmDialog = false
            },
            onDismiss = {
                showConfirmDialog = false
            }
        )
    }

    MeasurementScreenContent(
        measurements = measurements,
        listChartData = listChartData,
        date = date,
        measurementCardState = measurementCardState,
        bodyweight = bodyweight,
        fatMass = fatMass,
        leanMass = leanMass,
        notes = notes,
        measurementChart = measurementChart,
        updateBodyweight = viewModel::updateBodyweight,
        updateFatMass = viewModel::updateFatMass,
        updateLeanMass = viewModel::updateLeanMass,
        updateNotes = viewModel::updateNotes,
        showDatePickerDialog = { showDatePickerDialog = true },
        showConfirmDialog = {
            showConfirmDialog = true
            idMeasurementToDelete = it
        },
        updateIdMeasurement = viewModel::updateIdMeasurement,
        upsertMeasurement = viewModel::upsertMeasurementToDB,
        updateChartMode = viewModel::updateMeasurementChart,
        updateMeasurementCardState = viewModel::updateMeasurementCardState,
        navigateBack = navigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeasurementScreenContent(
    measurements: List<Measurement>,
    listChartData: List<ChartData>,
    bodyweight: Float,
    fatMass: Float,
    leanMass: Float,
    notes: String,
    date: LocalDateTime,
    measurementChart: MeasurementChart,
    measurementCardState: MeasurementCardState,
    updateBodyweight: (String) -> Unit,
    updateLeanMass: (String) -> Unit,
    updateFatMass: (String) -> Unit,
    updateNotes: (String) -> Unit,
    showDatePickerDialog: () -> Unit,
    showConfirmDialog: (Long) -> Unit,
    updateIdMeasurement: (Long) -> Unit,
    upsertMeasurement: () -> Unit,
    updateMeasurementCardState: (MeasurementCardState) -> Unit,
    updateChartMode: (MeasurementChart) -> Unit,
    navigateBack: () -> Unit,
) {
    val bodyweight = bodyweight.toString()
    val fatMass = fatMass.toString()
    val leanMass = leanMass.toString()


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
                                    if (measurementCardState == MeasurementCardState.NEW)
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
                            value = bodyweight,
                            label = { Text(text = stringResource(R.string.body_weight) + " *") },
                            suffix = { Text(stringResource(R.string.kg)) },
                            isError = bodyweight.isBlank() || bodyweight.toFloatOrNull() == 0f,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                showKeyboardOnFocus = true
                            ),
                            onValueChange = {
                                updateBodyweight(processFloatValue(it, 300f))
                            }
                        )
                        AnimatedVisibility(visible = isExpanded) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = fatMass,
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { updateFatMass("") }
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
                                        updateFatMass(processFloatValue(it, 100f))
                                    }
                                )

                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = leanMass,
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { updateLeanMass("") }
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
                                        updateLeanMass(processFloatValue(it, 100f))
                                    }
                                )
                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = Formatter.getShortDateFromLocalDate(date),
                                    onValueChange = {},
                                    label = { Text(stringResource(R.string.label_when)) },
                                    readOnly = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    trailingIcon = {
                                        IconButton(onClick = showDatePickerDialog) {
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
                                    onValueChange = updateNotes
                                )
                            }
                        }

                        Row {
                            LibreFitButton(
                                modifier = Modifier.weight(1f),
                                text = stringResource(
                                    if (measurementCardState == MeasurementCardState.NEW)
                                        R.string.add else R.string.save
                                ),
                                icon = ImageVector.vectorResource(
                                    if (measurementCardState == MeasurementCardState.NEW)
                                        R.drawable.ic_add else R.drawable.ic_edit
                                ),
                                enabled = bodyweight.isNotBlank() && bodyweight.toFloatOrNull() != 0f
                            ) {
                                upsertMeasurement()

                                focusManager.clearFocus()
                            }
                            AnimatedVisibility(measurementCardState == MeasurementCardState.EDIT) {
                                IconButton(
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        updateIdMeasurement(0)
                                        updateMeasurementCardState(MeasurementCardState.NEW)
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

            items(measurements, key = { it.id }) { m ->
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
                                    text = Formatter.getFullDateFromLocalDate(m.date)
                                        .replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    text = "${m.bodyWeight} " + stringResource(R.string.kg),
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row {
                                IconButton(
                                    onClick = {
                                        updateIdMeasurement(m.id)
                                        updateMeasurementCardState(MeasurementCardState.EDIT)


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
                                        showConfirmDialog(m.id)
                                    }
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_delete),
                                        contentDescription = null
                                    )
                                }
                            }
                        }

                        if (m.muscleMassPercentage != 0f || m.bodyFatPercentage != 0f) {
                            HorizontalDivider()
                        }

                        if (m.muscleMassPercentage != 0f) {
                            Text(
                                formatDetails(
                                    stringResource(R.string.lean_mass),
                                    m.muscleMassPercentage.toString() + " %"
                                )
                            )
                        }
                        if (m.bodyFatPercentage != 0f) {
                            Text(
                                formatDetails(
                                    stringResource(R.string.fat_mass),
                                    m.bodyFatPercentage.toString() + " %"
                                )
                            )
                        }

                        if (m.notes != "") {
                            HorizontalDivider()
                            Text(
                                formatDetails(stringResource(R.string.notes), m.notes)
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

    val idMeasurement = remember { mutableLongStateOf(0L) }

    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        MeasurementScreenContent(
            measurements = measurements,
            listChartData = measurements.map {
                ChartData(
                    yValues = listOf(
                        when (measurementChart) {
                            MeasurementChart.BODY_WEIGHT -> it.bodyWeight
                            MeasurementChart.FAT_MASS -> it.bodyFatPercentage
                            MeasurementChart.LEAN_MASS -> it.muscleMassPercentage
                        }
                    ),
                    xValue = it.date.format(shortDate)
                )
            },
            date = LocalDateTime.now(),
            measurementChart = measurementChart,
            showDatePickerDialog = {},
            showConfirmDialog = {},
            measurementCardState = MeasurementCardState.EDIT,
            updateIdMeasurement = { idMeasurement.longValue = it },
            upsertMeasurement = {},
            updateChartMode = {},
            bodyweight = 72f,
            fatMass = 0.12f,
            leanMass = 0.22f,
            notes = "This is a note",
            updateBodyweight = {},
            updateLeanMass = {},
            updateFatMass = {},
            updateNotes = {},
            updateMeasurementCardState = {},
            navigateBack = {}
        )
    }
}