pluginManagement {
    repositories {
        google()
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

rootProject.name = "ParTee"
include(":app")
include(":data")
include(":domain")
include(":features:login")
include(":features:core_ui")
include(":features:team")
include(":features:matching")
include(":features:recruit")
include(":features:group")
include(":features:screen")
