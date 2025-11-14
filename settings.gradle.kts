pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)  // ✅ Changed this
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ISKCONTemple"
include(":app")