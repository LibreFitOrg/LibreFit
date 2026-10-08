/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.di

import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.librefit.db.repository.DatasetRepository
import org.librefit.db.repository.MeasurementRepository
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.db.repository.WorkoutRepository
import org.librefit.di.qualifiers.ApplicationScope
import org.librefit.di.qualifiers.DefaultDispatcher
import org.librefit.di.qualifiers.IoDispatcher
import org.librefit.di.qualifiers.MainDispatcher
import org.librefit.helpers.DataHelper
import org.librefit.helpers.NotificationHelper
import org.librefit.helpers.SoundPlayer
import org.librefit.nav.Route
import org.librefit.services.WorkoutServiceManager
import org.librefit.ui.screens.beforeSaving.BeforeSavingScreenViewModel
import org.librefit.ui.screens.calendar.CalendarScreenViewModel
import org.librefit.ui.screens.editExercise.EditExerciseScreenViewModel
import org.librefit.ui.screens.editWorkout.EditWorkoutScreenViewModel
import org.librefit.ui.screens.exercises.ExercisesScreenViewModel
import org.librefit.ui.screens.home.HomeScreenViewModel
import org.librefit.ui.screens.infoExercise.InfoExerciseScreenViewModel
import org.librefit.ui.screens.infoWorkout.InfoWorkoutScreenViewModel
import org.librefit.ui.screens.measurements.MeasurementScreenViewModel
import org.librefit.ui.screens.profile.ProfileScreenViewModel
import org.librefit.ui.screens.settings.SettingsScreenViewModel
import org.librefit.ui.screens.shared.SharedViewModel
import org.librefit.ui.screens.statistics.StatisticsScreenViewModel
import org.librefit.ui.screens.workout.WorkoutScreenViewModel
import org.librefit.util.GlobalExceptionHandler

/**
 * Aggregate Koin module for LibreFit.
 *
 * Declares every application-scoped singleton (repositories, helpers, services) and all
 * ViewModel definitions. The included modules provide the database, DataStore, dispatchers,
 * and the application [kotlinx.coroutines.CoroutineScope].
 *
 * Conventions:
 * - Definitions whose constructor parameters are qualifier-decorated (dispatchers,
 *   application scope) use explicit lambdas — `viewModelOf`/`singleOf` constructor
 *   autowiring resolves by type only and cannot express per-parameter qualifiers.
 * - Route-assisted ViewModels declare their [Route] parameter inline in the definition;
 *   call sites supply it via `koinViewModel<VM> { parametersOf(route) }` (see the screens).
 */
val libreFitModules = module {
    includes(databaseModule, dataStoreModule, dispatcherModule, coroutineScopeModule)

    // Data layer — explicit lambdas where a qualifier-decorated dependency exists
    single { WorkoutRepository(get()) }
    single { MeasurementRepository(get()) }
    single {
        DatasetRepository(get(), get(named<ApplicationScope>()), get(), androidContext())
    }
    single {
        UserPreferencesRepository(get(), get(named<ApplicationScope>()), androidApplication())
    }

    // Helpers / services / util
    singleOf(::DataHelper)
    singleOf(::NotificationHelper)
    single { SoundPlayer(androidContext(), get(named<MainDispatcher>())) }
    singleOf(::WorkoutServiceManager)
    singleOf(::GlobalExceptionHandler)

    // ViewModels — plain dependencies only, resolved by type
    viewModelOf(::SharedViewModel)
    viewModelOf(::CalendarScreenViewModel)
    viewModelOf(::ExercisesScreenViewModel)
    viewModelOf(::HomeScreenViewModel)
    viewModelOf(::ProfileScreenViewModel)
    viewModelOf(::SettingsScreenViewModel)
    viewModelOf(::StatisticsScreenViewModel)

    // ViewModels with qualifier-decorated dependencies — explicit lambdas
    viewModel { MeasurementScreenViewModel(get(), get(named<DefaultDispatcher>()), get()) }

    // Route-assisted ViewModels — route supplied by the caller via parametersOf
    viewModel { (route: Route.BeforeSavingScreen) ->
        BeforeSavingScreenViewModel(
            route,
            get(),
            get(),
            get(),
            get(),
            get(named<IoDispatcher>()),
        )
    }
    viewModel { (route: Route.EditExerciseScreen) ->
        EditExerciseScreenViewModel(route, get(), get())
    }
    viewModel { (route: Route.EditWorkoutScreen) ->
        EditWorkoutScreenViewModel(route, get(), get(named<IoDispatcher>()), get())
    }
    viewModel { (route: Route.InfoExerciseScreen) ->
        InfoExerciseScreenViewModel(route, get(), get(), get(), get())
    }
    viewModel { (route: Route.InfoWorkoutScreen) ->
        InfoWorkoutScreenViewModel(route, get(), get(), get(named<IoDispatcher>()), get())
    }
    viewModel { (route: Route.WorkoutScreen) ->
        WorkoutScreenViewModel(
            route,
            get(),
            get(),
            get(),
            get(),
            get(),
            get(named<IoDispatcher>()),
            get(named<MainDispatcher>()),
        )
    }
}
