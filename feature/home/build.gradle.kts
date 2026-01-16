plugins {
    alias(libs.plugins.android.library)

    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.qodein.feature.home"
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
    implementation(projects.shared)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(projects.core.analytics)
    implementation(projects.core.data)

    implementation(libs.bundles.androidx.core)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.navigation)
    implementation(libs.bundles.image.loading)
    implementation(libs.bundles.koin)
    implementation(libs.haze)
    implementation(libs.androidx.browser)
    implementation(libs.timber)
    implementation(libs.kotlinx.datetime)

    testImplementation(libs.bundles.testing.unit)
    testImplementation(projects.core.testing)
    androidTestImplementation(libs.bundles.testing.android)
    debugImplementation(libs.bundles.debug)
}
