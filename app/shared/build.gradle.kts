import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.kotlin.serialization)
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
    
    android {
       namespace = "pl.meleko.trainspot.app.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
        }
        commonMain.dependencies {
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization.json)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.logging)
            implementation(libs.kotlinx.serialization.json)
            
            implementation(libs.androidx.datastore.preferences)
            
            api(projects.core)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.koin.androidx.compose)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewModel)

            implementation(libs.jetbrains.navigation3.ui)
            implementation(libs.jetbrains.navigation3.adaptive)

            implementation(libs.jetbrains.material.icons.extended)

            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            implementation(libs.napier)

            implementation(libs.krop.ui)
            implementation(libs.krop.filekit)

            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.filekit.coil)

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
    
    room {
        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    
    androidRuntimeClasspath(libs.compose.uiTooling)
}