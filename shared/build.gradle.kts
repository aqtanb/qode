import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

kotlin {
    jvmToolchain(17)
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)

            implementation(libs.kermit)
            implementation(libs.bundles.ktor)
            implementation(libs.koin.core)
        }

        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.firestore)
            implementation(libs.ktor.client.okhttp)
        }
    }
}

android {
    namespace = "com.qodein.shared"
    compileSdk =
        libs.versions.compileSdk
            .get()
            .toInt()

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
        val logoKey = localProperties.getProperty("LOGO_DEV_PUBLIC_KEY") ?: ""
        buildConfigField("String", "LOGO_DEV_PUBLIC_KEY", "\"$logoKey\"")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
