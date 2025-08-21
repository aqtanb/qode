plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.qodein.feature.search"
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
    // Shared multiplatform module
    implementation(project(":shared"))

    // Android-specific project modules
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(projects.core.analytics)

    implementation(projects.core.data)

    // Core Android & Compose
    implementation(libs.bundles.androidx.core)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.navigation)

    // Dependency Injection
    implementation(libs.bundles.hilt)
    implementation(libs.androidx.compose.animation.core)
    ksp(libs.hilt.compiler)

    // Testing
    testImplementation(libs.bundles.testing.unit)
    androidTestImplementation(libs.bundles.testing.android)

    // Debug Tools
    debugImplementation(libs.bundles.debug)

    // DateTime
    implementation(libs.kotlinx.datetime)

    // Image
    implementation(libs.bundles.image.loading)
}
