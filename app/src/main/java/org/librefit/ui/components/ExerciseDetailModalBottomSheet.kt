/*
 * Copyright (c) 2024. LibreFit
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
import org.librefit.enums.Muscle
import org.librefit.util.ExerciseDC
import org.librefit.util.exerciseEnumToStringId
import org.librefit.util.muscleToVectorId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailModalBottomSheet(
    exercise: ExerciseDC,
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

            HeadlineText(text = stringResource(id = R.string.details))


            if(exercise.force != null )
                Text(
                    formatDetails(
                        stringResource(R.string.force),
                        stringResource(exerciseEnumToStringId(exercise.force))
                )
            )
            Text(
                text = formatDetails(
                    stringResource(R.string.level),
                    stringResource(exerciseEnumToStringId(exercise.level))
                )
            )
            if(exercise.mechanic != null ) {
                Text(
                    formatDetails(
                        stringResource(R.string.mechanic),
                        stringResource(exerciseEnumToStringId(exercise.mechanic))
                    )
                )
            }
            if(exercise.equipment != null ) {
                Text(
                    formatDetails(
                        stringResource(R.string.equipment),
                        stringResource(exerciseEnumToStringId(exercise.equipment))
                    )
                )
            }
            Text(
                formatDetails(
                    stringResource(R.string.category),
                    stringResource(exerciseEnumToStringId(exercise.category))
                )
            )


            if (exercise.primaryMuscles.isNotEmpty() || exercise.secondaryMuscles.isNotEmpty()){
                HorizontalDivider()
                HeadlineText(text = stringResource(id = R.string.muscles))
            }

            if(exercise.primaryMuscles.isNotEmpty()){
                MuscleContent(
                    stringResource(id = R.string.primary_muscles),
                    musclesList = exercise.primaryMuscles
                )
            }


            if(exercise.secondaryMuscles.isNotEmpty()){
                MuscleContent(
                    stringResource(id = R.string.secondary_muscles),
                    musclesList = exercise.secondaryMuscles
                )
            }
            
            HorizontalDivider()

            HeadlineText(text = stringResource(id = R.string.instructions))

            Text(text = exercise.instructions.mapIndexed{ index, instruction->
                    "${index+1}. $instruction"
                }.joinToString("\n\n")
            )
        }
    }
}

@Composable
private fun MuscleContent(title : String, musclesList: List<Muscle>) {
    var list = ""
    musclesList.forEachIndexed { index , muscle->
        list += stringResource(exerciseEnumToStringId(muscle)) + if(index != musclesList.lastIndex) ", " else ""
    }
    Text(text = formatDetails(title, list) )
    LazyRow {
        items(musclesList){ muscle ->
            val vector = ImageVector.vectorResource(id = muscleToVectorId(muscle))
            Image(
                imageVector = vector,
                contentDescription = stringResource(exerciseEnumToStringId(muscle)),
                modifier = Modifier.size(150.dp)
            )
        }
    }
}

@Composable
private fun AlternatingImages(exercise: ExerciseDC){
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
            contentDescription = exercise.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .fillMaxSize()
        )
    }
}

private fun formatDetails(type: String, details : String) : AnnotatedString {
    return buildAnnotatedString {
        withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)){ append("$type: ") }
        append(details)
    }
}
