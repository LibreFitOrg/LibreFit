package org.librefit.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.R
import org.librefit.ui.components.HeadlineText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController : NavHostController) {

    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.label_about))
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
    ) { innerPadding ->

        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(start = 15.dp, end = 15.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){

            val size = 170.dp
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(size)
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceContainer),
                        RoundedCornerShape(size)
                    )
            )


            HeadlineText(text =  stringResource(R.string.label_help_us))

            AboutItem(Icons.Default.Favorite, stringResource(R.string.label_donate), null)

            AboutItem(
                ImageVector.vectorResource(R.drawable.ic_handshake),
                stringResource(R.string.label_contribute),
                null
            )

            AboutItem(
                ImageVector.vectorResource(R.drawable.ic_translate),
                stringResource(R.string.label_translate),
                null
            )


            HeadlineText(text = stringResource(R.string.label_info))

            AboutItem(
                ImageVector.vectorResource(R.drawable.ic_globe),
                stringResource(R.string.label_website),
                null
            )


            AboutItem(
                ImageVector.vectorResource(R.drawable.ic_source_code),
                stringResource(R.string.label_source_code),
                stringResource(R.string.label_url_source_code)
            )

            AboutItem(
                ImageVector.vectorResource(R.drawable.ic_policy),
                stringResource(R.string.label_privacy_policy),
                null
            )

            AboutItem(
                ImageVector.vectorResource(R.drawable.ic_license),
                stringResource(R.string.label_license),
                null
            )

        }
    }
}

@Composable
private fun AboutItem(
    imageVector: ImageVector,
    text: String,
    url: String? = null
){
    val context = LocalContext.current

    OutlinedCard(
        enabled = url != null,
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(url) }
            context.startActivity(intent)
        }
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ){
            Spacer(modifier = Modifier.width(20.dp))
            Icon(
                imageVector = imageVector,
                contentDescription = "$text icon"
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = text,
                fontSize = 20.sp
            )
        }
    }
}



@Preview
@Composable
private fun AboutScreenPreview(){
    AboutScreen(rememberNavController())
}