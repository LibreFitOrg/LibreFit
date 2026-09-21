/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.librefit.di.qualifiers.ApplicationScope
import org.librefit.di.qualifiers.DefaultDispatcher
import org.librefit.di.qualifiers.IoDispatcher
import org.librefit.di.qualifiers.MainDispatcher
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

/**
 * Runtime guard for the Koin container: actually starts the module graph and resolves the
 * qualifier-decorated bindings, instead of only static-checking them.
 *
 * [KoinModulesVerifyTest] type-checks definitions with `verify()`, but `verify()` never starts a
 * container — it cannot catch runtime loading/resolution bugs such as InsertKoinIO/koin#2381
 * (qualified definitions composed via `includes()` raising `NoDefinitionFoundException` at
 * runtime). This suite reproduces the exact failing lookup from the WorkoutScreenViewModel crash:
 * `get(named<MainDispatcher>())` against a container whose dispatchers come from an
 * `includes()`-ed [dispatcherModule].
 *
 * Plain JVM is sufficient: resolving dispatchers and the application scope never touches
 * `androidContext()` / `androidApplication()` definitions.
 */
class KoinRuntimeResolutionTest {

    @Test
    fun `main dispatcher resolves when dispatcherModule is registered directly`() {
        val application = koinApplication { modules(dispatcherModule) }
        try {
            assertSame(
                Dispatchers.Main,
                application.koin.get<CoroutineDispatcher>(named<MainDispatcher>())
            )
        } finally {
            application.close()
        }
    }

    @Test
    fun `qualified binding declared in the root module resolves`() {
        val qualifier = StringQualifier("librefit.test.control")
        val controlModule = module {
            single(qualifier) { "control-value" }
        }
        val application = koinApplication { modules(controlModule) }
        try {
            assertEquals("control-value", application.koin.get<String>(qualifier))
        } finally {
            application.close()
        }
    }

    @Test
    fun `application scope resolves from libreFitModules`() {
        val application = koinApplication { modules(libreFitModules) }
        try {
            assertNotNull(application.koin.get<CoroutineScope>(named<ApplicationScope>()))
        } finally {
            application.close()
        }
    }

    @Test
    fun `io dispatcher resolves from libreFitModules`() {
        val application = koinApplication { modules(libreFitModules) }
        try {
            assertSame(
                Dispatchers.IO,
                application.koin.get<CoroutineDispatcher>(named<IoDispatcher>())
            )
        } finally {
            application.close()
        }
    }

    @Test
    fun `default dispatcher resolves from libreFitModules`() {
        val application = koinApplication { modules(libreFitModules) }
        try {
            assertSame(
                Dispatchers.Default,
                application.koin.get<CoroutineDispatcher>(named<DefaultDispatcher>())
            )
        } finally {
            application.close()
        }
    }

    @Test
    fun `main dispatcher resolves from libreFitModules`() {
        val application = koinApplication { modules(libreFitModules) }
        try {
            assertSame(
                Dispatchers.Main,
                application.koin.get<CoroutineDispatcher>(named<MainDispatcher>())
            )
        } finally {
            application.close()
        }
    }

    @Test
    fun `main dispatcher resolves from a minimal module composed with includes`() {
        val consumerModule = module {
            includes(dispatcherModule)
            single { MainDispatcherConsumer(get(named<MainDispatcher>())) }
        }
        val application = koinApplication { modules(consumerModule) }
        try {
            assertSame(
                Dispatchers.Main,
                application.koin.get<MainDispatcherConsumer>().mainDispatcher
            )
        } finally {
            application.close()
        }
    }
}

/** Minimal consumer mirroring the production `SoundPlayer` wiring. */
private class MainDispatcherConsumer(val mainDispatcher: CoroutineDispatcher)
