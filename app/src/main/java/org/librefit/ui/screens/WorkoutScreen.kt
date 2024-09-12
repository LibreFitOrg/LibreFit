package org.librefit.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import org.librefit.R
import org.librefit.data.ExerciseDC
import org.librefit.data.SharedViewModel
import org.librefit.ui.components.ConfirmExitDialog
import org.librefit.ui.components.ExerciseDetailModalBottomSheet
import org.librefit.util.DataStoreManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    userPreferences: DataStoreManager,
    sharedViewModel: SharedViewModel,
    workoutId: Int,
    navController: NavHostController,
    list: List<ExerciseDC>
){
    val keepWorkoutScreenOn = userPreferences.workoutScreenOn.collectAsState(initial = true).value

    val context = LocalContext.current

    if(keepWorkoutScreenOn){
        DisposableEffect(key1 = Unit) {
            val window = (context as Activity).window

            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            onDispose {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    

    var showExitDialog by remember { mutableStateOf(false) }
    
    BackHandler (enabled = !showExitDialog){
        showExitDialog = true
    }

    if(showExitDialog){
        ConfirmExitDialog(
            text = stringResource(id = R.string.label_exit_workout),
            onExit = {
                navController.popBackStack()
                showExitDialog = false
            },
            onDismiss = { showExitDialog = false }
        )
    }
    
    

    val workoutWithExercises by sharedViewModel.getWorkoutWithExercises(workoutId).observeAsState()

    var isModalSheetOpen by remember { mutableStateOf(false) }

    /**
     * Used to display information about the selected exercise in [ExerciseDetailModalBottomSheet]
     */
    var selectedExercise by remember { mutableStateOf<ExerciseDC?>(null) }

    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    workoutWithExercises?.workout?.let {
                        Text(stringResource(R.string.label_workout) + ": " + it.title)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            showExitDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.label_navigate_back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Column (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Stopwatch()
                }
            }
        }
    ){
        LazyColumn (
            modifier = Modifier
                .padding(it)
                .padding(start = 15.dp, end = 15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ){
            workoutWithExercises?.let { workout ->
                items(workout.exercises){ exercise ->
                    list.forEach { item ->
                        if(item.id == exercise.exerciseId)
                            ExerciseCard(exercise = item) {
                                selectedExercise = item
                                isModalSheetOpen = true
                            }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(5.dp)) }
        }
    }

    if(isModalSheetOpen){
        ExerciseDetailModalBottomSheet(exercise = selectedExercise!!) { isModalSheetOpen = false }
    }
}

@Composable
private fun ExerciseCard(
    exercise: ExerciseDC,
    onDetail : () -> Unit
){
    var sets by remember { mutableIntStateOf(1) }

    ElevatedCard {
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
            }

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
                Spacer(modifier = Modifier.weight(1.2f))
                Text(text = stringResource(id = R.string.label_exercise_card_reps), color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.weight(1.3f))
                Text(text = stringResource(id = R.string.label_exercise_card_weight), color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = null,
                    modifier = Modifier.weight(1f)
                )
            }

            //Sets
            for (i in 1..sets){
                var checked by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topEndPercent = if (i == 1) 50 else 0,
                                topStartPercent = if (i == 1) 50 else 0,
                                bottomEndPercent = if (i == sets) 50 else 0,
                                bottomStartPercent = if (i == sets) 50 else 0
                            )
                        )
                        .background(if (checked) MaterialTheme.colorScheme.inversePrimary.copy(0.3f) else Color.Transparent)
                        .height(40.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(text = "$i")
                    Spacer(modifier = Modifier.weight(1f))
                    Checkbox(checked = checked, onCheckedChange = {checked = it})
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider()

            TextButton(
                onClick = { sets++ },
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null
                )
                Spacer( modifier = Modifier.weight(1f) )
                Text( text = stringResource(id = R.string.label_exercise_card_add) )
                Spacer( modifier = Modifier.weight(1.3f) )
            }

        }
    }
}

@Composable
private fun Stopwatch() {
    var elapsedTime by remember { mutableIntStateOf(0) }
    var isRunning by remember { mutableStateOf(true) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000) // Update every second
            elapsedTime++
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = formatTime(elapsedTime),
            modifier = Modifier.width(100.dp)
        )
        Row {
            FilledIconButton(
                onClick = { isRunning = !isRunning },
            ) {
                Icon(
                    imageVector = if (isRunning) {
                        ImageVector.vectorResource(id = R.drawable.ic_pause)
                    } else Icons.Default.PlayArrow ,
                    contentDescription = stringResource(id = if (isRunning) R.string.label_pause else R.string.label_play)
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(100.dp))
    }
}

private fun formatTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format(Locale.current.platformLocale,"%02d:%02d:%02d", hours, minutes, secs)
}