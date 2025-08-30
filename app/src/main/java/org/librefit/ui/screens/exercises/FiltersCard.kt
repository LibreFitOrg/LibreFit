/*
 * Copyright (c) 2024-2025. LibreFit
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

package org.librefit.ui.screens.exercises

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.librefit.R
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import org.librefit.enums.exercise.ExerciseProperty
import org.librefit.enums.exercise.FilterValue
import org.librefit.enums.exercise.Force
import org.librefit.enums.exercise.Level
import org.librefit.enums.exercise.Mechanic
import org.librefit.enums.exercise.Muscle
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter.exerciseEnumToStringId
import kotlin.reflect.KClass


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FiltersCard(
    isFilterExpanded: Boolean,
    updateCardExpansion: () -> Unit,
    updateFilter: (FilterValue) -> Unit,
    filterValue: FilterValue
) {
    val iconRotation = if (isFilterExpanded) 180f else 0f

    OutlinedCard(
        onClick = updateCardExpansion
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_filter),
                    contentDescription = stringResource(R.string.filters)
                )
                Text(
                    text = stringResource(R.string.filters),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            IconButton(
                onClick = {
                    updateCardExpansion()
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_drop_down),
                    contentDescription = stringResource(R.string.menu),
                    modifier = Modifier.rotate(iconRotation)
                )
            }
        }


        //Animation to display the filters
        AnimatedVisibility(visible = isFilterExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, end = 15.dp, bottom = 15.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ExerciseProperty.propertiesPairsByEnum.forEach { propertiesPair ->
                        ItemFilter(
                            pair = propertiesPair,
                            update = updateFilter,
                            value = filterValue
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemFilter(
    pair: Pair<List<ExerciseProperty?>, KClass<out ExerciseProperty>>,
    update: (FilterValue) -> Unit,
    value: FilterValue
) {
    val options: List<ExerciseProperty?> = pair.first

    val enumType = pair.second

    val propertyFilterValue: ExerciseProperty? = when (enumType) {
        Level::class -> value.level
        Force::class -> value.force
        Mechanic::class -> value.mechanic
        Muscle::class -> value.muscles
        Equipment::class -> value.equipment
        Category::class -> value.category
        else -> null
    }

    var expanded by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier.width(150.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(
                id = when (enumType) {
                    Level::class -> R.string.level
                    Force::class -> R.string.force
                    Mechanic::class -> R.string.mechanic
                    Muscle::class -> R.string.muscles
                    Equipment::class -> R.string.equipment
                    Category::class -> R.string.category
                    else -> R.string.any
                }
            )
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier
                .clickable {
                    expanded = !expanded
                    focusRequester.requestFocus()
                }
                .focusRequester(focusRequester)
                .focusable()
        ) {
            OutlinedTextField(
                shape = MaterialTheme.shapes.large,
                readOnly = true,
                value = stringResource(exerciseEnumToStringId(propertyFilterValue)),
                onValueChange = {},
                singleLine = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { enum ->
                    DropdownMenuItem(
                        onClick = {
                            when (enumType) {
                                Force::class -> update(value.copy(force = enum as Force?))
                                Level::class -> update(value.copy(level = enum as Level?))
                                Mechanic::class -> update(value.copy(mechanic = enum as Mechanic?))
                                Muscle::class -> update(value.copy(muscles = enum as Muscle?))
                                Equipment::class -> update(value.copy(equipment = enum as Equipment?))
                                Category::class -> update(value.copy(category = enum as Category?))
                                else -> {}
                            }
                            expanded = false
                        },
                        text = {
                            Text(
                                text = stringResource(exerciseEnumToStringId(enum))
                            )
                        },
                        trailingIcon = if (propertyFilterValue == enum) {
                            {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check),
                                    contentDescription = stringResource(R.string.checkbox)
                                )
                            }
                        } else null,
                        modifier = Modifier.background(
                            if (propertyFilterValue == enum) MaterialTheme.colorScheme.inversePrimary
                                .copy(0.3f) else Color.Unspecified
                        )
                    )
                }
            }
        }
    }


}

@Preview
@Composable
fun FiltersCardPreview() {
    var filterValue by remember { mutableStateOf(FilterValue()) }

    var isFilterExpanded by remember { mutableStateOf(true) }

    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        FiltersCard(
            isFilterExpanded = isFilterExpanded,
            updateCardExpansion = { isFilterExpanded = !isFilterExpanded },
            updateFilter = { filterValue = it },
            filterValue = filterValue
        )
    }
}