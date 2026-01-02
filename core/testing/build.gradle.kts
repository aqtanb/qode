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
    api(project(":shared"))
    api(projects.core.data)
    api(platform(libs.androidx.compose.bom))
    api(libs.kotlinx.datetime)
    api(libs.kotlinx.coroutines.test)
    api(libs.junit)
    api(libs.mockk) {
        exclude(group = "org.junit.jupiter")
    }
    api(libs.turbine)
    api(libs.androidx.junit)
    api(libs.androidx.espresso.core)
    api(libs.androidx.ui.test.junit4)
    api(libs.androidx.ui.test.manifest)
}
