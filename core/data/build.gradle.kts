import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.qodein.core.data"
    compileSdk =
        libs.versions.compileSdk
            .get()
            .toInt()

    defaultConfig {
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
        val properties =
            Properties().apply {
                load(rootProject.file("local.properties").inputStream())
            }
        buildConfigField("String", "WEB_CLIENT_ID", "\"${properties.getProperty("WEB_CLIENT_ID")}\"")
        resValue("string", "web_client_id", "${properties.getProperty("WEB_CLIENT_ID")}")
    }

    buildFeatures {
        buildConfig = true
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(projects.shared)
    implementation(projects.core.analytics)
    implementation(projects.core.notifications)

    implementation(libs.bundles.coroutines)
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)
    implementation(libs.bundles.authentication)
    implementation(libs.bundles.work)

    implementation(libs.bundles.koin)
    implementation(libs.datastore.preferences)
    implementation(libs.androidx.core.ktx)
    implementation(libs.timber)
    implementation(libs.kermit)
    implementation(libs.kotlinx.datetime)
}
