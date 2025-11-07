plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidLibrary {
        namespace = "com.qodein.shared"
        compileSdk =
            libs.versions.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kermit)
                implementation(libs.bundles.ktor)
                implementation(libs.koin.core)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                // Firebase BOM for version management
                implementation(project.dependencies.platform(libs.firebase.bom))
                // Firebase for expect/actual DocumentSnapshot
                implementation(libs.firebase.firestore)
                // Ktor Android engine
                implementation(libs.ktor.client.okhttp)
            }
        }
    }
}

// Use same JVM toolchain as other modules
kotlin {
    jvmToolchain(17)
    sourceSets.all {
        languageSettings.optIn("kotlin.time.ExperimentalTime")
    }
}
