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
        buildConfigField("String", "ALGOLIA_APP_ID", "\"${properties.getProperty("ALGOLIA_APP_ID")}\"")
        buildConfigField("String", "ALGOLIA_SEARCH_API_KEY", "\"${properties.getProperty("ALGOLIA_SEARCH_API_KEY")}\"")
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
    implementation(libs.algolia.client)
}
