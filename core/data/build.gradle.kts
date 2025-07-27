plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.qodein.core.data"
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
    implementation(projects.core.domain)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.xmaterial.ccp)

    // Coroutines
    implementation(libs.bundles.coroutines)

    // Android Context access
    implementation(libs.androidx.core.ktx)
}
