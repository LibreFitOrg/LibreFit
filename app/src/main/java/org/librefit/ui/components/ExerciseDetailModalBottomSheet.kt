package org.librefit.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.librefit.R
import org.librefit.data.Exercise
import org.librefit.data.Muscle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailModalBottomSheet(
    exercise: Exercise,
    onDismiss : () -> Unit
){
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
                Text(text = exercise.name, style = MaterialTheme.typography.headlineLarge )
            }

            HorizontalDivider()

            AlternatingImages(exercise = exercise)

            HorizontalDivider()

            HeadlineText(text = stringResource(id = R.string.label_details))


            if(exercise.force != null ) Text(text = enumHeadlines(stringResource(id = R.string.label_force), exercise.force))
            Text(text = enumHeadlines(stringResource(id = R.string.label_level), enum = exercise.level))
            if(exercise.mechanic != null ) Text(text = enumHeadlines(stringResource(id = R.string.label_mechanic), exercise.mechanic))
            if(exercise.equipment != null ) Text(text = enumHeadlines(stringResource(id = R.string.label_equipment), exercise.equipment))
            Text(text = enumHeadlines(stringResource(id = R.string.label_category), exercise.category))


            if (exercise.primaryMuscles.isNotEmpty() || exercise.secondaryMuscles.isNotEmpty()){
                HorizontalDivider()
                HeadlineText(text = stringResource(id = R.string.label_muscles))
            }

            if(exercise.primaryMuscles.isNotEmpty()){
                MuscleContent(stringResource(id = R.string.label_primary_muscles), musclesList = exercise.primaryMuscles)
            }


            if(exercise.secondaryMuscles.isNotEmpty()){
                MuscleContent(stringResource(id = R.string.label_secondary_muscles), musclesList = exercise.secondaryMuscles)
            }
            
            HorizontalDivider()
            
            HeadlineText(text = stringResource(id = R.string.label_instructions))

            Text(text = exercise.instructions
                .mapIndexed{ index, instruction->
                    "${index+1}. $instruction"
                }.joinToString("\n\n")
            )
        }
    }
}

private fun enumHeadlines(type : String, enum: Enum<*>? = null, listEnum: List<Enum<*>>? = emptyList()) : AnnotatedString {
    val result = buildAnnotatedString {
        withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)){ append("$type: ") }
        append(formatExerciseEnums(enum = enum, listEnum = listEnum))
    }
    return result
}

private fun formatExerciseEnums(listEnum : List<Enum<*>>? = emptyList(), enum : Enum<*>? = null) : String? {

    if (listEnum != emptyList<Enum<*>>() && listEnum != null) {
        return listEnum.map { it.name }.joinToString(", ") { word -> word.lowercase().replace("_"," ").replaceFirstChar { it.uppercaseChar() } }
    }

    if (enum != null) {
        return enum.name.lowercase().replace("_"," ").replaceFirstChar { it.uppercaseChar() }
    }

    return null
}


@Composable
private fun AlternatingImages(exercise: Exercise){
    val firstBitmap = BitmapFactory.decodeStream(LocalContext.current.assets.open(exercise.images[0]))
    val secondBitmap = BitmapFactory.decodeStream(LocalContext.current.assets.open(exercise.images[1]))

    // State to hold the current image bitmap
    var currentBitmap by remember { mutableStateOf(firstBitmap) }

    // LaunchedEffect to change the image every second
    LaunchedEffect(Unit) {
        var i = 0
        while (true) {
            delay(1000)
            i++
            currentBitmap = if(i % 2 == 0) firstBitmap else secondBitmap //Alternate images every second
        }
    }

    // Display the current image
    currentBitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .fillMaxSize()
        )
    }
}

@Composable
private fun MuscleContent(title : String, musclesList: List<Muscle>) {
    Text(text = enumHeadlines(title, listEnum =  musclesList))
    LazyRow {
        items(musclesList){ muscle ->
            val vector = when(muscle){
                Muscle.ABDOMINALS -> ImageVector.vectorResource(id = R.drawable.abdominals)
                Muscle.ABDUCTORS -> ImageVector.vectorResource(id = R.drawable.abductors)
                Muscle.ADDUCTORS -> ImageVector.vectorResource(id = R.drawable.adductors)
                Muscle.BICEPS -> ImageVector.vectorResource(id = R.drawable.biceps)
                Muscle.CALVES -> ImageVector.vectorResource(id = R.drawable.calves)
                Muscle.CHEST -> ImageVector.vectorResource(id = R.drawable.chest)
                Muscle.FOREARMS -> ImageVector.vectorResource(id = R.drawable.forearms)
                Muscle.GLUTES -> ImageVector.vectorResource(id = R.drawable.glutes)
                Muscle.HAMSTRINGS -> ImageVector.vectorResource(id = R.drawable.harmstring)
                Muscle.LATS -> ImageVector.vectorResource(id = R.drawable.lats)
                Muscle.LOWER_BACK -> ImageVector.vectorResource(id = R.drawable.lower_back)
                Muscle.MIDDLE_BACK -> ImageVector.vectorResource(id = R.drawable.middle_back)
                Muscle.NECK -> ImageVector.vectorResource(id = R.drawable.neck)
                Muscle.QUADRICEPS -> ImageVector.vectorResource(id = R.drawable.quads)
                Muscle.SHOULDERS -> ImageVector.vectorResource(id = R.drawable.shoulders)
                Muscle.TRAPS -> ImageVector.vectorResource(id = R.drawable.traps)
                Muscle.TRICEPS -> ImageVector.vectorResource(id = R.drawable.triceps)
            }
            Image(imageVector = vector, contentDescription = "", modifier = Modifier.size(150.dp))
        }
    }
}