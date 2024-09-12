package org.librefit.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.R
import org.librefit.data.ExerciseDC
import org.librefit.data.SharedViewModel
import org.librefit.ui.components.ConfirmExitDialog
import org.librefit.ui.components.ExerciseDetailModalBottomSheet
import org.librefit.util.exerciseEnumToStringId


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseScreen(
    list: List<ExerciseDC>,
    navigateBack : () -> Unit,
    viewModel: SharedViewModel
){
    val selectedExercisesList = remember { mutableStateListOf<ExerciseDC>() }

    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler (enabled = !showExitDialog && selectedExercisesList.isNotEmpty()){
        showExitDialog = true
    }

    if(showExitDialog){
        ConfirmExitDialog(
            text = stringResource(id = R.string.label_exit_add_exercise),
            onExit = {
                navigateBack()
                showExitDialog = false
                viewModel.resetFilterList()
            },
            onDismiss = { showExitDialog = false }
        )
    }


    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.label_add_exercise))
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if(selectedExercisesList.isNotEmpty()){
                                showExitDialog = true
                            } else {
                                navigateBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.label_navigate_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        enabled = selectedExercisesList.isNotEmpty(),
                        onClick = {
                            viewModel.addExerciseList(selectedExercisesList)
                            navigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = Icons.Default.Add.name
                        )
                    }
                }
            )
        }
    ){ innerPadding ->
        AddExerciseScreenContent(innerPadding, list, selectedExercisesList, viewModel)
    }
}

@Composable
private fun AddExerciseScreenContent(
    innerPadding: PaddingValues,
    exerciseList: List<ExerciseDC>,
    selectedExercisesList: MutableList<ExerciseDC>,
    viewModel: SharedViewModel,
) {

    val isFilterExpanded = remember { mutableStateOf(false) }

    /**
     * Used to display information about the selected exercise using [ExerciseDetailModalBottomSheet]
     */
    var selectedExercise by remember { mutableStateOf<ExerciseDC?>(null) }

    var isModalSheetOpen by remember { mutableStateOf(false) }

    // Query used to search an exercises based on the name
    var query by remember { mutableStateOf("") }



    val filteredExercisesList = exerciseList.filter { exercise ->
        exercise.name.contains(query, ignoreCase = true) &&
        viewModel.isEnumInList(exercise.level) &&
        viewModel.isEnumInList(exercise.category)
//        viewModel.isEnumInList(exercise.force!!) &&
//        viewModel.isEnumInList(exercise.equipment!!) &&
//        viewModel.isEnumInList(exercise.mechanic!!)
    }

    LazyColumn (
        modifier = Modifier.padding(innerPadding)
    ){
        // Search bar
        item {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                TextField(
                    value = query,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp),
                    onValueChange = {
                        query = it
                    },
                    shape = RoundedCornerShape(40.dp),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if(query.isNotEmpty()){
                            IconButton(onClick = { query = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = Icons.Default.Close.name)
                            }
                        }
                    },
                    label = { Text(text = stringResource(id = R.string.label_search_exercise_field)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor =  Color.Transparent,
                        unfocusedIndicatorColor =  Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )
            }
        }


        //This is temporary until FiltersCard is ready
        item { Spacer(modifier = Modifier.height(15.dp)) }

        // Card to let the user filter the exercises list TODO: it doesn't work properly yet
        // item { FiltersCard(isFilterExpanded = isFilterExpanded, viewModel = viewModel) }

        if(filteredExercisesList.isNotEmpty()) {
            item { HorizontalDivider() }
        } else {
            item{
                Column (
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ){
                    Text(
                        text = stringResource(id = R.string.label_no_exercise_found),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        //Filtered list of exercises
        items(
            items = filteredExercisesList,
            key = { exercise -> exercise.id }
        ) { exercise ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = selectedExercisesList.contains(exercise),
                    onCheckedChange = {
                        if (selectedExercisesList.contains(exercise)) {
                            selectedExercisesList.remove(exercise)
                        } else {
                            selectedExercisesList.add(exercise)
                        }
                    }
                )
                Column (
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ){
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = stringResource(id = exerciseEnumToStringId(exercise.category)),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (exercise.equipment != null){
                        Text(
                            text = stringResource(id = exerciseEnumToStringId(exercise.equipment)),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                IconButton(
                    onClick = {
                        selectedExercise = exercise
                        isModalSheetOpen = true
                    }
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = Icons.Default.Info.name)
                }
            }
            HorizontalDivider()
        }
    }

    // Opened by info icon (in the filtered list), it shows the details of an exercise
    if(isModalSheetOpen){
        ExerciseDetailModalBottomSheet(exercise = selectedExercise!!) { isModalSheetOpen = false }
    }
}


@Preview
@Composable
private fun AddExerciseScreenPreview(){
    AddExerciseScreen(emptyList(), {}, viewModel())
}