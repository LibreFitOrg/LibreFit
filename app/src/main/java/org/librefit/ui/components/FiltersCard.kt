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

package org.librefit.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.librefit.R
import org.librefit.enums.Category
import org.librefit.enums.Equipment
import org.librefit.enums.Force
import org.librefit.enums.Level
import org.librefit.enums.Mechanic
import org.librefit.enums.Muscle
import org.librefit.ui.screens.shared.SharedViewModel
import org.librefit.util.exerciseEnumToStringId

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FiltersCard(
    isFilterExpanded: MutableState<Boolean>,
    viewModel: SharedViewModel
) {
    //TODO: move FiltersCard to ExercisesScreen and remove method from SharedViewModel
    var iconRotation by rememberSaveable { mutableFloatStateOf(0f) }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp),
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
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_filter),
                    contentDescription = stringResource(R.string.filters)
                )
                Text(
                    text = stringResource(R.string.filters),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            IconButton(
                onClick = {
                    isFilterExpanded.value = !isFilterExpanded.value
                    iconRotation = if (isFilterExpanded.value) 180f else 0f
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = stringResource(R.string.menu),
                    modifier = Modifier.rotate(iconRotation)
                )
            }
        }


        val titles = listOf(
            R.string.level,
            R.string.force,
            R.string.mechanic,
            R.string.equipment,
            R.string.muscles,
            R.string.category
        )

        val options: List<List<Enum<*>?>> = listOf(
            Level.entries + null,
            Force.entries + null,
            Mechanic.entries + null,
            Equipment.entries + null,
            Muscle.entries + null,
            Category.entries + null
        ).map {
            //This will move the last element (null) in the first place
            listOf(it.last()) + it.dropLast(1)
        }



        //Animation to display the filters
        AnimatedVisibility(visible = isFilterExpanded.value) {
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
                    for(i in 0..5){
                        ItemFilter(
                            title = stringResource(titles[i]),
                            options = options[i],
                            changeEnum = {
                                viewModel.updateFilter(it, i)
                            },
                            enumFilterValue = viewModel.getFilter(i)
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
    title: String,
    options: List<Enum<*>?>,
    changeEnum: (Enum<*>?) -> Unit,
    enumFilterValue: Enum<*>?
) {
    var expanded by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier.width(150.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title)
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
            TextField(
                readOnly = true,
                value = stringResource(
                    if (enumFilterValue == null) R.string.any else exerciseEnumToStringId(
                        enumFilterValue
                    )
                ),
                onValueChange = {},
                singleLine = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                colors = ExposedDropdownMenuDefaults.textFieldColors()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { enum ->
                    val enumString = stringResource(
                        if (enum == null) R.string.any else exerciseEnumToStringId(enum)
                    )
                    DropdownMenuItem(
                        onClick = {
                            changeEnum(enum)
                            expanded = false
                        },
                        text = {
                            Text(
                                text = enumString
                            )
                        },
                        trailingIcon = if (enumFilterValue == enum) {
                            {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = stringResource(R.string.checkbox)
                                )
                            }
                        } else null,
                        modifier = Modifier.background(
                            if (enumFilterValue == enum) MaterialTheme.colorScheme.inversePrimary.copy(0.3f) else Color.Unspecified
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
    FiltersCard(remember { mutableStateOf(true) }, viewModel())
}