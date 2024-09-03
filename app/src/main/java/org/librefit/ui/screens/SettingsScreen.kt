package org.librefit.ui.screens

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.librefit.R
import org.librefit.ui.components.HeadlineText
import org.librefit.util.DataStoreManager
import org.librefit.util.Language
import org.librefit.util.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController, userPreferences: DataStoreManager) {
    val selectedTheme = userPreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM).value

    val keepWorkoutScreenOn = userPreferences.workoutScreenOn.collectAsState(initial = true).value

    val materialModeOn = userPreferences.materialMode.collectAsState(initial = true).value

    var selectedLanguage by remember { mutableStateOf(AppCompatDelegate.getApplicationLocales().toLanguageTags()) }

    var openPreferenceDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()


    if(openPreferenceDialog){
        AlertDialog(
            title = { Text(stringResource(id = R.string.label_language)) },
            onDismissRequest = { openPreferenceDialog = false },
            confirmButton = { /*The user doesn't need to confirm*/ },
            text = {
                LazyColumn{
                    items(Language.entries){ language ->
                        Row (
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            RadioButton(
                                selected = language.code == selectedLanguage,
                                onClick = {
                                    selectedLanguage = language.code
                                    val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(language.code)
                                    AppCompatDelegate.setApplicationLocales(appLocale)
                                }
                            )
                            Text(text = stringResource(id = languageCodeToId(language.code) ))
                        }
                    }
                }
            }
        )
    }

    Scaffold (
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.label_settings))
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = Icons.AutoMirrored.Default.ArrowBack.name
                        )
                    }
                }
            )
        },
    ){ innerPadding ->
        LazyColumn (
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ){


            item { HeadlineText(text = stringResource(id = R.string.label_appearance)) }

            item {
                Column {
                    Row {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_dark_mode),
                            contentDescription = null,
                            modifier = Modifier.padding(start = 15.dp, end = 15.dp)
                        )
                        Text(text = stringResource(id = R.string.label_theme), style = MaterialTheme.typography.titleMedium)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
                        SingleChoiceSegmentedButtonRow {
                            ThemeMode.entries.forEachIndexed { index, mode ->
                                SegmentedButton(
                                    selected = selectedTheme == mode,
                                    onClick = { coroutineScope.launch { userPreferences.savePreference(userPreferences.themeModeKey , index) } },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = ThemeMode.entries.size)
                                ) { Text( stringResource(id = themeModeToId(mode))  ) }
                            }

                        }
                    }
                }
            }

            item {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                    Row (
                        modifier = Modifier
                            .height(70.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Row (verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_material),
                                contentDescription = "",
                                modifier = Modifier.padding(start = 15.dp, end = 15.dp)
                            )
                            Column (verticalArrangement = Arrangement.Center){
                                Text(text = stringResource(id = R.string.label_material_you), style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = stringResource(
                                        id = if(materialModeOn) R.string.label_dynamic_color_enabled else R.string.label_dynamic_color_disabled
                                    ) ,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        Switch(
                            checked = materialModeOn,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences.savePreference(userPreferences.materialModeKey, it)
                                }
                            }
                        )
                    }
                }
            }

            item {
                Row (
                    modifier = Modifier
                        .height(70.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Row (verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_keep),
                            contentDescription = "",
                            modifier = Modifier.padding(start = 15.dp, end = 15.dp)
                        )
                        Column(Modifier.weight(1f)){
                            Text(
                                text = stringResource(id = R.string.label_keep_screen_on),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text =  stringResource(
                                    id= if(keepWorkoutScreenOn) R.string.label_screen_on_desc else R.string.label_screen_off_desc
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    Switch(
                        checked = keepWorkoutScreenOn,
                        onCheckedChange = {
                            coroutineScope.launch {
                                userPreferences.savePreference(userPreferences.keepOnWorkoutScreenKey, it)
                            }
                        }
                    )
                }
            }



            item { HeadlineText(text = stringResource(id = R.string.label_settings_general)) }

            item{
                Row (
                    modifier = Modifier
                        .height(70.dp)
                        .fillMaxWidth()
                        .clickable { openPreferenceDialog = true },
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_translate),
                        contentDescription = null,
                        modifier = Modifier.padding(start = 15.dp, end = 15.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(id = R.string.label_language),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(id = languageCodeToId(selectedLanguage) ),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}


private fun themeModeToId(themeMode: ThemeMode) : Int {
    val id = when(themeMode){
        ThemeMode.SYSTEM -> R.string.label_follow_system
        ThemeMode.LIGHT -> R.string.label_theme_light
        ThemeMode.DARK -> R.string.label_theme_dark
    }
    return id
}

private fun languageCodeToId( code : String ) : Int {
    val result = when(code){
        "en" -> R.string.label_language_english
        "it" -> R.string.label_language_italian
        else -> R.string.label_follow_system
    }

    return result
}

@Preview
@Composable
fun SettingsScreenPreview(){
    SettingsScreen(rememberNavController(), DataStoreManager(LocalContext.current))
}