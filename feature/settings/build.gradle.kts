plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.aboutlibraries)
}

android {
    namespace = "com.qodein.feature.settings"
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
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "VERSION_NAME", "\"${libs.versions.versionName.get()}\"")
        buildConfigField("int", "VERSION_CODE", libs.versions.versionCode.get())
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
    implementation(libs.bundles.aboutlibraries)
    implementation(libs.kermit)
    implementation(libs.bundles.hilt)
    ksp(libs.hilt.compiler)

    testImplementation(libs.bundles.testing.unit)
    testImplementation(projects.core.testing)
    androidTestImplementation(projects.core.testing)
    debugImplementation(libs.bundles.debug)
}
