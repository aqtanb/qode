plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.qodein.core.ui"
    compileSdk =
        libs.versions.compileSdk
            .get()
            .toInt()

    defaultConfig {
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Project modules
    implementation(projects.core.designsystem)
    implementation(projects.shared)

    // Core Android
    implementation(libs.androidx.core.ktx)

    // DateTime
    implementation(libs.kotlinx.datetime)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)

    // Coil
    implementation(libs.bundles.image.loading)

    // Blur Effects
    implementation(libs.haze)

    // Color Analysis
    implementation(libs.androidx.palette)

    // Browser (CustomTabs)
    implementation(libs.androidx.browser)

    // Logging
    implementation(libs.timber)

    // Testing
    testImplementation(libs.bundles.testing.unit)
    androidTestImplementation(libs.bundles.testing.android)
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Debug Tools
    debugImplementation(libs.bundles.debug)
}
