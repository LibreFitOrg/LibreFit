/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.di

import android.app.Application
import android.content.Context
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.Assert.assertThrows
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.verify.MissingKoinDefinitionException
import org.koin.test.verify.definition
import org.koin.test.verify.injectedParameters
import org.koin.test.verify.verify
import org.librefit.di.qualifiers.MainDispatcher
import org.librefit.nav.Route
import org.librefit.ui.screens.beforeSaving.BeforeSavingScreenViewModel
import org.librefit.ui.screens.editExercise.EditExerciseScreenViewModel
import org.librefit.ui.screens.editWorkout.EditWorkoutScreenViewModel
import org.librefit.ui.screens.infoExercise.InfoExerciseScreenViewModel
import org.librefit.ui.screens.infoWorkout.InfoWorkoutScreenViewModel
import org.librefit.ui.screens.workout.WorkoutScreenViewModel

/**
 * Build-time guard for the Koin dependency graph: type-checks every definition in
 * [libreFitModules] (including included modules) without starting the container.
 *
 * Koin resolves dependencies at runtime; this test is the compile-time safety net that
 * catches missing or ambiguous bindings before the app starts. Extend [libreFitModules]
 * and this test together: new parameter-declared definitions (e.g. route-assisted
 * ViewModels) need a matching `definition<VM>(Param::class)` entry below.
 */
@OptIn(KoinExperimentalAPI::class)
class KoinModulesVerifyTest {

    @Test
    fun `all modules type-check`() {
        libreFitModules.verify(
            // Types bound by the Android Koin context at runtime, not by explicit definitions
            extraTypes = listOf(
                Context::class,
                Application::class,
            ),
            // Route-assisted ViewModels receive their Route via parametersOf at the call site
            injections = injectedParameters(
                definition<BeforeSavingScreenViewModel>(Route.BeforeSavingScreen::class),
                definition<EditExerciseScreenViewModel>(Route.EditExerciseScreen::class),
                definition<EditWorkoutScreenViewModel>(Route.EditWorkoutScreen::class),
                definition<InfoExerciseScreenViewModel>(Route.InfoExerciseScreen::class),
                definition<InfoWorkoutScreenViewModel>(Route.InfoWorkoutScreen::class),
                definition<WorkoutScreenViewModel>(Route.WorkoutScreen::class),
            ),
        )
    }

    @Test
    fun `verify() passes when the dispatcher module is included`() {
        // Positive control: a qualifier-decorated CoroutineDispatcher dependency is
        // satisfied once dispatcherModule is part of the graph (the fixed wiring).
        val moduleWithDispatchers = module {
            includes(dispatcherModule)
            single { DispatcherConsumer(get(named<MainDispatcher>())) }
        }

        moduleWithDispatchers.verify()
    }

    @Test
    fun `verify() catches a missing qualifier-decorated dispatcher dependency`() {
        // Regression guard for the WorkoutScreenViewModel crash: resolving a
        // @MainDispatcher CoroutineDispatcher must fail verification when no module
        // provides the qualified definition.
        val brokenModule = module {
            single { DispatcherConsumer(get(named<MainDispatcher>())) }
        }

        val exception =
            assertThrows(MissingKoinDefinitionException::class.java) { brokenModule.verify() }

        assertThat(exception).hasMessageThat().contains("CoroutineDispatcher")
    }
}

/** Test double that requires a qualifier-decorated [CoroutineDispatcher]. */
@Suppress("unused")
private class DispatcherConsumer(mainDispatcher: CoroutineDispatcher)
