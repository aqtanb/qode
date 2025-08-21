plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.qodein.feature.promocode"
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

    implementation(projects.core.data)

    // Core Android & Compose
    implementation(libs.bundles.androidx.core)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.navigation)

    // DateTime
    implementation(libs.kotlinx.datetime)

    implementation(libs.bundles.image.loading)

    // Dependency Injection
    implementation(libs.bundles.hilt)
    ksp(libs.hilt.compiler)

    // Testing
    testImplementation(libs.bundles.testing.unit)
    testImplementation(projects.core.testing)
    androidTestImplementation(projects.core.testing)

    // Debug Tools
    debugImplementation(libs.bundles.debug)
}
