/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.measurements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.librefit.R
import org.librefit.db.entity.Measurement
import org.librefit.enums.MeasurementCardState
import org.librefit.enums.chart.MeasurementChart
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitButton
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.EmptyLottie
import org.librefit.ui.components.charts.LibreFitCartesianChart
import org.librefit.ui.components.charts.Point
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
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementScreen(
    navigateBack: () -> Unit
) {
    val viewModel: MeasurementScreenViewModel = hiltViewModel()

    val measurements by viewModel.measurements.collectAsStateWithLifecycle()

    val points by viewModel.points.collectAsStateWithLifecycle()

    val measurementChart by viewModel.measurementChart.collectAsStateWithLifecycle()

    val bodyweight by viewModel.bodyWeight.collectAsStateWithLifecycle()

    val leanMass by viewModel.leanMass.collectAsStateWithLifecycle()

    val fatMass by viewModel.fatMass.collectAsStateWithLifecycle()

    val notes by viewModel.notes.collectAsStateWithLifecycle()

    val measurementCardState by viewModel.measurementCardState.collectAsStateWithLifecycle()

    val date by viewModel.date.collectAsStateWithLifecycle()

    val datePickerState = rememberDatePickerState()
    val showDatePickerDialog = remember { mutableStateOf(false) }

    if (showDatePickerDialog.value) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog.value = false },
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

    val idMeasurementToDelete = remember { mutableStateOf<Long?>(null) }

    idMeasurementToDelete.value?.let {
        ConfirmDialog(
            title = stringResource(R.string.delete_measurement_question),
            text = stringResource(R.string.delete_measurement_text),
            confirmText = stringResource(R.string.delete),
            onConfirm = {
                viewModel.deleteMeasurementById(it)
            },
            onDismiss = {
                idMeasurementToDelete.value = null
            }
        )
    }

    MeasurementScreenContent(
        measurements = measurements,
        listChartData = points,
        date = date,
        measurementCardState = measurementCardState,
        bodyweight = bodyweight,
        fatMass = fatMass?.toString() ?: "",
        leanMass = leanMass?.toString() ?: "",
        notes = notes,
        measurementChart = measurementChart,
        updateBodyweight = viewModel::updateBodyweight,
        updateFatMass = viewModel::updateFatMass,
        updateLeanMass = viewModel::updateLeanMass,
        updateNotes = viewModel::updateNotes,
        showDatePickerDialog = { showDatePickerDialog.value = true },
        showConfirmDialog = {
            idMeasurementToDelete.value = it
        },
        updateIdMeasurement = viewModel::updateIdMeasurement,
        upsertMeasurement = viewModel::upsertMeasurementToDB,
        updateChartMode = viewModel::updateMeasurementChart,
        updateMeasurementCardState = viewModel::updateMeasurementCardState,
        navigateBack = navigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MeasurementScreenContent(
    measurements: List<Measurement>,
    listChartData: List<Point>,
    bodyweight: String,
    fatMass: String,
    leanMass: String,
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
                    points = listChartData,
                    chartMode = measurementChart,
                    updateChartMode = { updateChartMode(it as MeasurementChart) }
                )
            }

            // Add/edit measurement card
            item {
                var isExpanded by rememberSaveable { mutableStateOf(false) }

                OutlinedCard(
                    shape = MaterialTheme.shapes.extraLarge
                ) {
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
                                style = MaterialTheme.typography.headlineSmallEmphasized
                            )

                            IconButton(
                                onClick = {
                                    isExpanded = !isExpanded
                                }
                            ) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                            }
                        }

                        Row {
                            OutlinedTextField(
                                shape = MaterialTheme.shapes.large,
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester),
                                value = bodyweight,
                                label = { Text(text = stringResource(R.string.body_weight)) },
                                suffix = { Text(stringResource(R.string.kg)) },
                                isError = bodyweight.isBlank(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    showKeyboardOnFocus = true
                                ),
                                onValueChange = updateBodyweight
                            )
                            Spacer(Modifier.width(10.dp))
                            OutlinedTextField(
                                shape = MaterialTheme.shapes.large,
                                modifier = Modifier.weight(1f),
                                value = Formatter.getShortDateFromLocalDate(date),
                                onValueChange = {},
                                label = { Text(stringResource(R.string.label_when)) },
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = showDatePickerDialog) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_date_range),
                                            contentDescription = stringResource(R.string.select_date)
                                        )
                                    }
                                }
                            )
                        }
                        AnimatedVisibility(visible = isExpanded) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row {
                                    OutlinedTextField(
                                        shape = MaterialTheme.shapes.large,
                                        modifier = Modifier.weight(1f),
                                        value = fatMass,
                                        trailingIcon = {
                                            IconButton(
                                                onClick = { updateFatMass("") }
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_cancel),
                                                    contentDescription = null
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
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number
                                        ),
                                        onValueChange = updateFatMass
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    OutlinedTextField(
                                        shape = MaterialTheme.shapes.large,
                                        modifier = Modifier.weight(1f),
                                        value = leanMass,
                                        trailingIcon = {
                                            IconButton(
                                                onClick = { updateLeanMass("") }
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_cancel),
                                                    contentDescription = null
                                                )
                                            }
                                        },
                                        label = { Text(stringResource(R.string.lean_mass)) },
                                        suffix = { Text("%") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number
                                        ),
                                        onValueChange = updateLeanMass
                                    )
                                }

                                OutlinedTextField(
                                    shape = MaterialTheme.shapes.large,
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
                                icon = painterResource(
                                    if (measurementCardState == MeasurementCardState.NEW)
                                        R.drawable.ic_add else R.drawable.ic_edit
                                ),
                                enabled = bodyweight.isNotBlank() && bodyweight.toDoubleOrNull() != 0.0
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
                                    Icon(painterResource(R.drawable.ic_cancel), null)
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
                ElevatedCard(
                    modifier = Modifier.animateItem(),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
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
                            val interactionSources =
                                remember { List(2) { MutableInteractionSource() } }
                            ButtonGroup(
                                overflowIndicator = {}
                            ) {
                                customItem(
                                    buttonGroupContent = {
                                        IconButton(
                                            interactionSource = interactionSources[0],
                                            modifier = Modifier.animateWidth(interactionSources[0]),
                                            shapes = IconButtonDefaults.shapes(),
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
                                                painter = painterResource(R.drawable.ic_edit),
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    menuContent = {}
                                )
                                customItem(
                                    buttonGroupContent = {
                                        IconButton(
                                            interactionSource = interactionSources[1],
                                            modifier = Modifier.animateWidth(interactionSources[1]),
                                            shapes = IconButtonDefaults.shapes(),
                                            onClick = {
                                                showConfirmDialog(m.id)
                                            }
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_delete),
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    menuContent = {}
                                )
                            }
                        }

                        if (m.muscleMassPercentage != 0 || m.bodyFatPercentage != 0) {
                            HorizontalDivider()
                        }

                        if (m.muscleMassPercentage != 0) {
                            Text(
                                formatDetails(
                                    stringResource(R.string.lean_mass),
                                    m.muscleMassPercentage.toString() + " %"
                                )
                            )
                        }
                        if (m.bodyFatPercentage != 0) {
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
        }
    }
}

@Preview
@Composable
private fun MeasurementScreenPreview() {
    val shortDate: DateTimeFormatter? = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.SHORT)
        .withLocale(LocalLocale.current.platformLocale)

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
                bodyWeight = Random.nextDouble(60.0, 80.0),
                bodyFatPercentage = if (Random.nextBoolean()) Random.nextInt(10, 80) else 0,
                muscleMassPercentage = if (Random.nextBoolean()) Random.nextInt(20, 80) else 0,
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

    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        MeasurementScreenContent(
            measurements = measurements,
            listChartData = measurements.map {
                Point(
                    yValues = listOf(
                        when (measurementChart) {
                            MeasurementChart.BODY_WEIGHT -> it.bodyWeight
                            MeasurementChart.FAT_MASS -> it.bodyFatPercentage
                            MeasurementChart.LEAN_MASS -> it.muscleMassPercentage
                        }.toDouble()
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
            bodyweight = "72",
            fatMass = "12",
            leanMass = "22",
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