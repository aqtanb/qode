enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "qode"
include(":androidApp")
include(":core:designsystem")
include(":core:ui")
include(":feature:auth")
include(":feature:home")
include(":core:data")
include(":feature:post")
include(":feature:profile")
include(":core:testing")
include(":feature:promocode")
include(":feature:settings")
include(":shared")
include(":core:analytics")
include(":core:notifications")
include(":feature:report")
include(":feature:block")
include(":feature:service")
