pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    
}
rootProject.name = "before_you_die"

include(":android")
include(":desktop")
include(":common")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("kotlinx-coroutines", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
        }
    }
}