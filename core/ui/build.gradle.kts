import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

private val localProperties =
    Properties().apply {
        load(rootProject.file("local.properties").inputStream())
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
        buildConfig = true
    }

    buildTypes {
        debug {
            resValue(
                "string",
                "web_client_id",
                localProperties.getProperty("WEB_CLIENT_ID_DEBUG")
                    ?: localProperties.getProperty("WEB_CLIENT_ID")
                    ?: "",
            )
        }
        release {
            resValue(
                "string",
                "web_client_id",
                localProperties.getProperty("WEB_CLIENT_ID") ?: "",
            )
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.shared)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.kotlinx.datetime)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.image.loading)
    implementation(libs.haze)
    implementation(libs.androidx.palette)
    implementation(libs.androidx.browser)
    implementation(libs.timber)
    implementation(libs.kermit)
    implementation(libs.androidx.exifinterface)
    implementation(libs.koin.core)

    testImplementation(libs.bundles.testing.unit)
    testImplementation(projects.core.testing)
    androidTestImplementation(libs.bundles.testing.android)
    androidTestImplementation(projects.core.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.bundles.debug)
}
