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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.R
import org.librefit.data.Exercise
import org.librefit.data.SharedViewModel
import org.librefit.nav.Destination
import org.librefit.ui.components.ConfirmExitDialog
import org.librefit.ui.components.ExerciseDetailModalBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineScreen(
    navController: NavHostController,
    viewModel : SharedViewModel
) {
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler (enabled = !showExitDialog && viewModel.addedExercisesList.isNotEmpty() ){
        showExitDialog = true
    }

    if(showExitDialog){
        ConfirmExitDialog(
            text = stringResource(id = R.string.label_exit_create_routine),
            onExit = {
                navController.popBackStack()
                showExitDialog = false
                viewModel.resetList()
            },
            onDismiss = { showExitDialog = false }
        )
    }

    val ableToSave = remember { mutableStateOf(false) }

    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.label_routine))
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (viewModel.addedExercisesList.isNotEmpty()) {
                                showExitDialog = true
                            } else {
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = Icons.AutoMirrored.Default.ArrowBack.name
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            //TODO Waiting for ROOM implementation
                            navController.popBackStack()
                        },
                        enabled = ableToSave.value && viewModel.addedExercisesList.isEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = Icons.Default.Done.name
                        )
                    }
                }
            )
        }
    ){ innerPadding ->
        RoutineScreen(innerPadding, navController, viewModel, ableToSave)
    }
}

@Composable
private fun RoutineScreen(
    innerPadding: PaddingValues,
    navController: NavHostController,
    viewModel: SharedViewModel,
    ableToSave: MutableState<Boolean>
) {
    var titleRoutine by rememberSaveable { mutableStateOf("") }

    var isModalSheetOpen by remember { mutableStateOf(false) }

    //Used to display information about the selected exercise in the modal bottom sheet
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues = innerPadding)
            .padding(start = 15.dp, end = 15.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item{
            OutlinedTextField(
                value = titleRoutine,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                onValueChange = { newTitle ->
                    titleRoutine = newTitle
                    ableToSave.value = titleRoutine.isNotEmpty()
                },
                trailingIcon = {
                    if(titleRoutine.isEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = Icons.Default.Warning.name
                        )
                    }
                },
                isError = titleRoutine.isEmpty(),
                label = { Text(text = stringResource(id = R.string.label_text_field_title)) },
                colors = OutlinedTextFieldDefaults.colors()
            )
        }
        if(viewModel.addedExercisesList.isEmpty()){
            item {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_launcher_monochrome),
                    contentDescription = ""
                )
            }
            item{
                Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
                    Text(
                        text = stringResource(id = R.string.label_start_creating_routine),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        } else {
//            val exercise = Exercise("1", "Pull up", null,
//                Level.BEGINNER,null,null,listOf(Muscle.LATS),
//                listOf(Muscle.LATS), listOf(""), Category.CARDIO, listOf("0","1")
//            )
//            item{
//                ExerciseCard(exercise = exercise, viewModel = viewModel){
//                    selectedExercise = exercise
//                    isModalSheetOpen = true
//                }
//            }
            items(viewModel.addedExercisesList){ exercise ->
                ExerciseCard(
                    exercise = exercise,
                    onDetail = {
                        selectedExercise = exercise
                        isModalSheetOpen = true
                    },
                    onDelete = { viewModel.removeExercise(exercise) }
                )
            }
        }

        item{
            TextButton(
                onClick = { navController.navigate(Destination.AddExerciseScreen) },
                colors = ButtonDefaults.buttonColors()
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = Icons.Default.AddCircle.name
                )
                Spacer( modifier = Modifier.weight(1f) )
                Text( text = stringResource(id = R.string.label_add_exercise) )
                Spacer( modifier = Modifier.weight(1.3f) )
            }
        }
    }

    // Opened by info icon (in the exercise card), it shows the details of an exercise
    if(isModalSheetOpen){
        ExerciseDetailModalBottomSheet(exercise = selectedExercise!!) { isModalSheetOpen = false }
    }
}


@Composable
private fun ExerciseCard(
    exercise: Exercise,
    onDetail : () -> Unit,
    onDelete : () -> Unit
) {
    var sets by remember {mutableIntStateOf(1)}

    var note by remember { mutableStateOf("") }

    ElevatedCard  (
        modifier = Modifier.fillMaxWidth()
    ){
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onDetail() }) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = Icons.Default.Info.name)
                }
                IconButton(onClick = { onDelete() }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = Icons.Default.Delete.name)
                }
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(id = R.string.label_notes))},
                value = note,
                onValueChange = {note = it}
            )

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider()

            //Headline set
            Row(
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = stringResource(id = R.string.label_exercise_card_set), color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = stringResource(id = R.string.label_exercise_card_reps), color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = stringResource(id = R.string.label_exercise_card_weight), color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(12.dp))
            }

            //Sets
            for (i in 1..sets){
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(text = "$i")
                }
            }

            HorizontalDivider()

            TextButton(
                onClick = { sets++ },
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = Icons.Default.AddCircle.name
                )
                Spacer( modifier = Modifier.weight(1f) )
                Text( text = stringResource(id = R.string.label_exercise_card_add) )
                Spacer( modifier = Modifier.weight(1.3f) )
            }
        }
    }
}

@Preview
@Composable
private fun CreateRoutineScreenPreview(){
    CreateRoutineScreen( rememberNavController() , viewModel() )
}