plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.qodein.core.domain"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
    }
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(projects.core.model)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Coroutines
    implementation(libs.bundles.coroutines)
}
