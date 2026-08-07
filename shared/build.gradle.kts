import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.sqldelight)
}

// ─────────────────────────────────────────────────────────────────────────────
// SESIÓN 3 — API key de TMDB SIN subirla a Git
// Leemos `tmdb.apikey` de local.properties (que está en .gitignore) y con Gradle
// generamos un archivo Kotlin con esa key como constante. Flujo:
//   local.properties  →  Gradle (aquí)  →  CineBuildConfig.TMDB_API_KEY (en el código)
// Si la key sale vacía en la app, es que falta la línea `tmdb.apikey=...` en local.properties.
// ─────────────────────────────────────────────────────────────────────────────
val tmdbApiKey: String = run {
    val props = Properties()
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { props.load(it) }
    props.getProperty("tmdb.apikey").orEmpty()
}

// Tarea de Gradle que ESCRIBE el archivo CineBuildConfig.kt dentro de build/generated.
// Al depender de esta tarea desde commonMain, se regenera sola en cada build.
val generateCineBuildConfig by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/cine/commonMain/kotlin")
    outputs.dir(outputDir)
    val apiKey = tmdbApiKey // capturamos el valor (amigable con el configuration-cache)
    doLast {
        val pkgDir = outputDir.get().dir("com/jetbrains/kmpapp/config").asFile
        pkgDir.mkdirs()
        pkgDir.resolve("CineBuildConfig.kt").writeText(
            """
            package com.jetbrains.kmpapp.config

            /**
             * Constantes de configuración GENERADAS por Gradle en tiempo de compilación.
             * La API key viene de local.properties (ignorado por Git): el secreto nunca viaja al repo.
             * ¿La ves vacía? Pega `tmdb.apikey=TU_KEY` en local.properties y vuelve a sincronizar.
             */
            object CineBuildConfig {
                const val TMDB_API_KEY: String = "$apiKey"
                const val TMDB_BASE_URL: String = "https://api.themoviedb.org/3/"
                const val TMDB_IMAGE_BASE_URL: String = "https://image.tmdb.org/t/p/w500"
            }
            """.trimIndent()
        )
    }
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    androidLibrary {
        namespace = "com.jetbrains.kmpapp.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
    }

    sourceSets {
        // Añadimos el código generado (CineBuildConfig.kt) como fuente de commonMain.
        commonMain {
            kotlin.srcDir(generateCineBuildConfig)
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqldelight.android.driver) // driver de SQLite para Android
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)  // driver de SQLite para iOS (Native)
        }
        commonMain.dependencies {
            implementation(libs.sqldelight.runtime)               // API común de SQLDelight
            implementation(libs.sqldelight.coroutines.extensions) // .asFlow() → observar la DB como Flow
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)

            implementation(libs.navigation.compose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.compose.material.icons.core)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)
        }
    }
    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.ktor.client.mock) // MockEngine para probar la red sin internet
    }
}


dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

// ─────────────────────────────────────────────────────────────────────────────
// SESIÓN 4 — SQLDelight
// A partir de los archivos .sq de commonMain/sqldelight/, SQLDelight GENERA código
// Kotlin type-safe: una clase `CineDb` y las funciones de las queries (selectAll, etc.).
// packageName define en qué paquete quedan esas clases generadas.
// ─────────────────────────────────────────────────────────────────────────────
sqldelight {
    databases {
        create("CineDb") {
            packageName.set("com.jetbrains.kmpapp.db")
        }
    }
}
