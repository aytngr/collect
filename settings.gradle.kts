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
        google()
        mavenCentral()
    }
}

rootProject.name = "TaskFlow"
include(":app")
include(":core")
include(":domain")
include(":data")
include(":feature")
include(":core:common")
include(":feature:notes")
include(":feature:home")
include(":feature:note-detail")
include(":feature:overlay")
include(":core:ui")
include(":core:navigation")
include(":core:designsystem")
