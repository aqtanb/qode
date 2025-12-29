import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.aboutlibraries)
}

android {
    namespace = "com.qodein.qode"
    compileSdk =
        libs.versions.compileSdk
            .get()
            .toInt()

    val properties =
        Properties().apply {
            load(rootProject.file("local.properties").inputStream())
        }

    defaultConfig {
        applicationId = "com.qodein.qode"
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.targetSdk
                .get()
                .toInt()
        versionCode =
            libs.versions.versionCode
                .get()
                .toInt()
        versionName = libs.versions.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file(properties.getProperty("KEYSTORE_FILE"))
            storePassword = properties.getProperty("KEYSTORE_PASSWORD")
            keyAlias = properties.getProperty("KEY_ALIAS")
            keyPassword = properties.getProperty("KEY_PASSWORD")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "qode-debug")
            resValue(
                "string",
                "web_client_id",
                properties.getProperty("WEB_CLIENT_ID_DEBUG")
                    ?: properties.getProperty("WEB_CLIENT_ID")
                    ?: "",
            )
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
            resValue(
                "string",
                "web_client_id",
                properties.getProperty("WEB_CLIENT_ID")
                    ?: "",
            )
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.add("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
}

dependencies {
    implementation(projects.shared)

    implementation(projects.core.designsystem)
    implementation(projects.core.data)
    implementation(projects.core.ui)
    implementation(projects.core.analytics)
    implementation(projects.core.notifications)

    implementation(projects.feature.auth)
    implementation(projects.feature.block)
    implementation(projects.feature.home)
    implementation(projects.feature.inbox)
    implementation(projects.feature.post)
    implementation(projects.feature.profile)
    implementation(projects.feature.promocode)
    implementation(projects.feature.report)
    implementation(projects.feature.settings)

    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)
    implementation(libs.bundles.androidx.core)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.navigation)
    implementation(libs.bundles.work)
    implementation(libs.bundles.authentication)
    implementation(libs.bundles.hilt)
    ksp(libs.hilt.compiler)

    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.androidx.startup)
    implementation(libs.kermit)
    implementation(libs.timber)

    testImplementation(projects.core.testing)
    androidTestImplementation(projects.core.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.bundles.debug)
}
