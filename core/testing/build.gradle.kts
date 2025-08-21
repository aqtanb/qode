plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.qodein.core.testing"
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
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Core modules - expose as API so test modules can use them
    api(project(":shared"))

    // Compose BOM for consistent versions
    api(platform(libs.androidx.compose.bom))

    // Coroutines testing
    api(libs.kotlinx.coroutines.test)

    // Test frameworks
    api(libs.junit)
    api(libs.mockk) {
        exclude(group = "org.junit.jupiter")
    }
    api(libs.turbine)

    // Android testing
    api(libs.androidx.junit)
    api(libs.androidx.espresso.core)

    // Compose testing
    api(libs.androidx.ui.test.junit4)
    api(libs.androidx.ui.test.manifest)
}
