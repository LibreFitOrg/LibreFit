/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.activities

import android.app.PendingIntent
import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.librefit.R
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.ui.components.LibreFitButton
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.WarningLottie
import org.librefit.ui.theme.LibreFitTheme
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

class ErrorActivity : ComponentActivity() {

    private val restartIntent: PendingIntent? by lazy {
        IntentCompat.getParcelableExtra(intent,EXTRA_RESTART_PENDING_INTENT, PendingIntent::class.java)
    }

    companion object {
        const val EXTRA_STACK_TRACE = "EXTRA_STACK_TRACE"
        const val EXTRA_RESTART_PENDING_INTENT = "EXTRA_RESTART_PENDING_INTENT"
        const val EXTRA_THEME_MODE = "EXTRA_THEME_MODE"
        const val EXTRA_MATERIAL_MODE = "EXTRA_MATERIAL_MODE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val stackTrace = intent.getStringExtra(EXTRA_STACK_TRACE) ?: ""
        val theme =
            IntentCompat.getSerializableExtra(intent, EXTRA_THEME_MODE, ThemeMode::class.java)
                ?: ThemeMode.SYSTEM
        val dynamicColor = intent.getBooleanExtra(EXTRA_MATERIAL_MODE, false)

        setContent {
            LibreFitTheme(
                dynamicColor = dynamicColor,
                themeMode = theme
            )  {
                ErrorScreen(
                    stackTrace = stackTrace,
                    onRestart = {
                        restartIntent?.send()
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ErrorScreen(
    stackTrace: String,
    onRestart: () -> Unit,
) {
    val context = LocalContext.current

    val coroutine = rememberCoroutineScope()

    val clipboardManager = LocalClipboard.current

    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if(copied) {
            // Display check icon instead of copy icon for 3 seconds after stack trace is copied
            delay(3000.milliseconds)
            copied = false
        }
    }


    val searchGitHubLink =
        stringResource(R.string.url_github_search_issue) + (stackTrace.lines().firstOrNull() ?: "")

    LibreFitScaffold { paddingValues ->
        LibreFitLazyColumn(
            innerPadding = paddingValues,
            verticalSpacing = 25.dp
        ) {
            item {
                WarningLottie()
            }
            item {
                Text(
                    text = stringResource(R.string.application_encountered_error),
                    style = MaterialTheme.typography.displaySmall
                )
            }
            item {
                val interactionSources = remember { List(2) { MutableInteractionSource() } }
                ButtonGroup(
                    overflowIndicator = {}
                ) {
                    customItem(
                        buttonGroupContent = {
                            LibreFitButton(
                                text = stringResource(R.string.restart_app),
                                onClick = onRestart,
                                modifier = Modifier
                                    .weight(0.4f)
                                    .animateWidth(interactionSources[0]),
                                icon = painterResource(R.drawable.ic_refresh),
                                interactionSource = interactionSources[0]
                            )
                        },
                        menuContent = {}
                    )
                    customItem(
                        buttonGroupContent = {
                            LibreFitButton(
                                text = stringResource(R.string.report_github),
                                icon = painterResource(R.drawable.ic_bug_report),
                                elevated = false,
                                modifier = Modifier
                                    .weight(0.6f)
                                    .animateWidth(interactionSources[1]),
                                enabled = stackTrace.isNotEmpty(),
                                interactionSource = interactionSources[1]
                            ) {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = searchGitHubLink.toUri()
                                }
                                context.startActivity(intent)
                            }
                        },
                        menuContent = {}
                    )
                }
            }

            item {
                ElevatedCard  {
                    Column(
                        modifier = Modifier.padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row (
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Text(
                                text = stringResource(R.string.crash_log),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(
                                enabled = stackTrace.isNotEmpty(),
                                onClick = {
                                    coroutine.launch {
                                        val clipData =
                                            ClipData.newPlainText("Copied Url", stackTrace)
                                        clipboardManager.setClipEntry(ClipEntry(clipData))
                                        copied = true
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(
                                        if(copied) R.drawable.ic_check else R.drawable.ic_copy
                                    ),
                                    contentDescription = null
                                )
                            }
                        }
                        LazyRow {
                            item {
                                Text(
                                    text = stackTrace.takeIf { it != "" }
                                        ?: stringResource(R.string.stack_trace_not_available),
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }

        }
    }
}

@Preview
@Composable
private fun ErrorScreenPreview() {
    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        ErrorScreen(
            stackTrace = if (Random.nextBoolean())
                "This is a very long long long long long stack trace\n".repeat(50) else "",
            onRestart = {}
        )
    }
}