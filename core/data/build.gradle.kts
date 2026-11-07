
plugins {
    alias(libs.plugins.android.library)
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
    }

    buildTypes {
        debug {
            buildConfigField("String", "SUPABASE_URL", "\"https://sjfzxrnqqlgywwqofdhu.supabase.co\"")
            buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", "\"sb_publishable_9BBbE8omnJDmEZASS77SlA_u9ZwmObp\"")
        }
        release {
            buildConfigField("String", "SUPABASE_URL", "\"https://hirfattxysizfprsydyq.supabase.co\"")
            buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", "\"sb_publishable_UJEfBc9H1vKX-QXIsJI_7A_JnT_UB9M\"")
        }
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
    implementation(libs.bundles.authentication)
    implementation(libs.bundles.work)

    implementation(libs.bundles.koin)
    implementation(libs.koin.androidx.workmanager)
    implementation(libs.datastore.preferences)
    implementation(libs.androidx.core.ktx)
    implementation(libs.timber)
    implementation(libs.kermit)
    implementation(libs.kotlinx.datetime)

    testImplementation(libs.bundles.testing.unit)
    testImplementation(libs.kotlinx.serialization.json)
}
