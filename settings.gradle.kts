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
include(":feature:search")
include(":feature:inbox")
include(":feature:profile")
include(":core:testing")
include(":feature:promocode")
include(":feature:settings")
include(":shared")
include(":core:analytics")
