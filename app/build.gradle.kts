/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion.VERSION_17
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.about.libraries)
}


val versionProps = Properties().apply {
    load(FileInputStream(rootProject.file("version.properties")))
}
val vMajor = versionProps.getProperty("major").toInt()
val vMinor = versionProps.getProperty("minor").toInt()
val vPatch = versionProps.getProperty("patch").toInt()
val vBuild = versionProps.getProperty("build").toInt()

configure<ApplicationExtension> {
    namespace = "org.librefit"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    lint {
        checkGeneratedSources = false
    }

    defaultConfig {
        applicationId = "org.librefit.app"
        minSdk = 26
        targetSdk = 36

        versionName = "$vMajor.$vMinor.$vPatch"
        versionCode = (vMajor * 1_000_000) + (vMinor * 10_000) + (vPatch * 100) + vBuild

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        androidResources {
            localeFilters.addAll(listOf("en", "it"))
        }

        // Do not use System.currentTimeMillis()
        val timestamp = System.getenv("SOURCE_DATE_EPOCH")?.toLongOrNull() ?: 1700000000L
        buildConfigField("long", "BUILD_TIME", "${timestamp}L")

        // Disable vector->PNG generation (legacy adapter), which is non-deterministic
        vectorDrawables.generatedDensities()
    }

    buildTypes {
        release {
            // Disable VCS Info (AGP 8.3+).
            // If the git repo isn't perfectly clean, this injects diffs
            vcsInfo.include = false

            // It is a common source of instability
            isShrinkResources = true

            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
        }
        sourceSets {
            named("release") {
                kotlin.directories += "build/generated/ksp/release/kotlin"
                kotlin.directories += "build/generated/ksp/release/java"
            }
            named("debug") {
                kotlin.directories += "build/generated/ksp/debug/kotlin"
                kotlin.directories += "build/generated/ksp/debug/java"
            }
        }
    }
    compileOptions {
        sourceCompatibility = VERSION_17
        targetCompatibility = VERSION_17
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

dependencyLocking {
    lockAllConfigurations()
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlinx.serialization.json)

    // Unit test
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk.android)
    testImplementation(libs.mockk.agent)

    // Instrumented test
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)


    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Navigation
    implementation(libs.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    ksp(libs.androidx.room.compiler)

    // Kotlin Extensions and Coroutines support for Room
    implementation(libs.androidx.room.ktx)

    // DataStore
    implementation(libs.androidx.datastore.core.android)
    implementation(libs.androidx.datastore.preferences)

    // Google accompanist to handle permissions in jetpack compose
    implementation(libs.accompanist.permissions)

    // Lottie animations for jetpack compose
    implementation(libs.lottie.compose)

    // Splash screen API
    implementation(libs.androidx.core.splashscreen)

    // M3 Compose vico charts
    implementation(libs.compose.m3)

    // Dagger - Hilt for dependency injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)


    // AboutLibraries to show used dependencies in jetpack compose
    implementation(libs.aboutlibraries.compose.m3)

    // Used in ui models' properties
    implementation(libs.kotlinx.collections.immutable)

    // Used to apply material colors to splash screen
    implementation(libs.material)

    // Load images asynchronously
    implementation(libs.coil)
}