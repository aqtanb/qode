import java.util.Properties

plugins {
    alias(libs.plugins.android.library)

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
        resValues = true
    }

    buildTypes {
        debug {
            resValue(
                "string",
                "web_client_id",
                localProperties.getProperty("WEB_CLIENT_ID_DEBUG")
                    ?: "539742119334-kqmh13gabo3ugepfn5m79p13790r9tjs.apps.googleusercontent.com"
                    ?: "",
            )
        }
        release {
            resValue(
                "string",
                "web_client_id",
                localProperties.getProperty("WEB_CLIENT_ID")
                    ?: "371926698326-ug3ui9ihutq4898v7ku0cllankdroqjk.apps.googleusercontent.com",
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
    implementation(libs.koin.android)

    testImplementation(libs.bundles.testing.unit)
    testImplementation(projects.core.testing)
    androidTestImplementation(libs.bundles.testing.android)
    androidTestImplementation(projects.core.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.bundles.debug)
}
