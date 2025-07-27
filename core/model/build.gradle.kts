plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.qodein.core.model"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        jvmToolchain(11)
    }
}

dependencies {
    // Pure domain models - no dependencies needed initially
    // Add only when you need specific utilities like:
    // implementation(libs.kotlinx.datetime) // for date/time models
    implementation(libs.kotlinx.serialization.json)
}
